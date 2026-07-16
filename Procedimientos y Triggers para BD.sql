--Regenerá la BD desde Power Architect, y corré en este orden:
--esquema (Power Architect)  →  Procedimientos y Triggers para BD.sql  →  Inserts inciales.sql
--El orden importa: al insertar los factura_compra_detalle, el trigger de stock ya 
--debe existir para que stock se popule solo (art 1 y art 8 en depósito 1; art 6 en depósito 2).


-- Trigger: sumar al stock cuando se inserta un detalle con articulo + deposito
  CREATE OR REPLACE FUNCTION public.fn_factura_compra_detalle_stock_insert()
  RETURNS TRIGGER AS $$
  BEGIN
      -- Solo actúa si el detalle tiene artículo Y depósito
      -- (gasto/fondoFijo no tienen articulo → no afecta stock)
      IF NEW.id_articulo IS NULL OR NEW.id_deposito IS NULL THEN
          RETURN NEW;
      END IF;

      -- UPSERT atómico: si no hay fila (deposito, articulo), la crea con min/max=0
      INSERT INTO public.stock (id_deposito, id_articulo, stk_cantidad_minima, stk_cantidad_maxima, stk_stock_actual)
      VALUES (NEW.id_deposito, NEW.id_articulo, 0, 0, NEW.fact_comp_cantidad)
      ON CONFLICT (id_deposito, id_articulo)
      DO UPDATE SET stk_stock_actual = public.stock.stk_stock_actual + EXCLUDED.stk_stock_actual;

      RETURN NEW;
  END;
  $$ LANGUAGE plpgsql;

  CREATE TRIGGER trg_factura_compra_detalle_stock_ins
  AFTER INSERT ON public.factura_compra_detalle
  FOR EACH ROW
  EXECUTE PROCEDURE public.fn_factura_compra_detalle_stock_insert();

  -- Trigger: revertir stock cuando la factura pasa a 'Anulado'
  CREATE OR REPLACE FUNCTION public.fn_factura_compra_estado_anular_stock()
  RETURNS TRIGGER AS $$
  DECLARE
      d RECORD;
  BEGIN
      -- Solo cuando estado pasa de no-anulado a 'Anulado'
      IF NEW.fact_comp_estado <> 'Anulado' OR OLD.fact_comp_estado = 'Anulado' THEN
          RETURN NEW;
      END IF;

      FOR d IN
          SELECT id_articulo, id_deposito, fact_comp_cantidad
          FROM public.factura_compra_detalle
          WHERE id_fact_comp_cab = NEW.id_fact_comp_cab
            AND id_articulo IS NOT NULL
            AND id_deposito IS NOT NULL
      LOOP
          UPDATE public.stock
          SET stk_stock_actual = stk_stock_actual - d.fact_comp_cantidad
          WHERE id_deposito = d.id_deposito AND id_articulo = d.id_articulo;
      END LOOP;

      RETURN NEW;
  END;
  $$ LANGUAGE plpgsql;

  CREATE TRIGGER trg_factura_compra_estado_anular
  AFTER UPDATE OF fact_comp_estado ON public.factura_compra_cabecera
  FOR EACH ROW
  WHEN (NEW.fact_comp_estado IS DISTINCT FROM OLD.fact_comp_estado)
  EXECUTE PROCEDURE public.fn_factura_compra_estado_anular_stock();

  -- =====================================================================================
  -- NOTA DE CRÉDITO DE COMPRA (DEVOLUCIÓN) — espejo del de factura, pero RESTA stock.
  -- Discriminador = id_deposito: línea con artículo + depósito = devolución física;
  -- línea sin depósito = NC financiera (descuento/bonificación) → no toca stock.
  -- Ver NOTA_CREDITO_DEBITO_PLAN.md §5.3.
  -- =====================================================================================

  -- Trigger: RESTAR del stock cuando se inserta un detalle de NC con articulo + deposito
  CREATE OR REPLACE FUNCTION public.fn_nota_credito_compra_detalle_stock_insert()
  RETURNS TRIGGER AS $$
  BEGIN
      -- Solo actúa si la línea tiene artículo Y depósito (devolución física de mercadería).
      -- Las líneas financieras (sin depósito) no afectan stock.
      IF NEW.id_articulo IS NULL OR NEW.id_deposito IS NULL THEN
          RETURN NEW;
      END IF;

      -- UPSERT atómico: RESTA la cantidad devuelta (signo opuesto al de factura).
      -- Si no existe la fila (deposito, articulo), la crea con el negativo (defensa;
      -- la validación Java "devuelta <= comprada" debería evitar este borde).
      INSERT INTO public.stock (id_deposito, id_articulo, stk_cantidad_minima, stk_cantidad_maxima, stk_stock_actual)
      VALUES (NEW.id_deposito, NEW.id_articulo, 0, 0, -NEW.nota_cred_comp_cantidad)
      ON CONFLICT (id_deposito, id_articulo)
      DO UPDATE SET stk_stock_actual = public.stock.stk_stock_actual + EXCLUDED.stk_stock_actual;

      RETURN NEW;
  END;
  $$ LANGUAGE plpgsql;

  CREATE TRIGGER trg_nota_credito_compra_detalle_stock_ins
  AFTER INSERT ON public.nota_credito_compra_detalle
  FOR EACH ROW
  EXECUTE PROCEDURE public.fn_nota_credito_compra_detalle_stock_insert();

  -- Trigger: REPONER stock cuando la NC pasa a 'Anulado' (reversa idempotente)
  CREATE OR REPLACE FUNCTION public.fn_nota_credito_compra_estado_anular_stock()
  RETURNS TRIGGER AS $$
  DECLARE
      d RECORD;
  BEGIN
      -- Solo cuando estado pasa de no-anulado a 'Anulado'
      IF NEW.nota_cred_comp_estado <> 'Anulado' OR OLD.nota_cred_comp_estado = 'Anulado' THEN
          RETURN NEW;
      END IF;

      FOR d IN
          SELECT id_articulo, id_deposito, nota_cred_comp_cantidad
          FROM public.nota_credito_compra_detalle
          WHERE id_nota_cred_comp_cab = NEW.id_nota_cred_comp_cab
            AND id_articulo IS NOT NULL
            AND id_deposito IS NOT NULL
      LOOP
          -- Reponer: suma de vuelta lo que la NC había restado.
          UPDATE public.stock
          SET stk_stock_actual = stk_stock_actual + d.nota_cred_comp_cantidad
          WHERE id_deposito = d.id_deposito AND id_articulo = d.id_articulo;
      END LOOP;

      RETURN NEW;
  END;
  $$ LANGUAGE plpgsql;

  CREATE TRIGGER trg_nota_credito_compra_estado_anular
  AFTER UPDATE OF nota_cred_comp_estado ON public.nota_credito_compra_cabecera
  FOR EACH ROW
  WHEN (NEW.nota_cred_comp_estado IS DISTINCT FROM OLD.nota_cred_comp_estado)
  EXECUTE PROCEDURE public.fn_nota_credito_compra_estado_anular_stock();