-- Cuenta a pagar por comprobante: cada nota de credito/debito de compra tiene su propia fila en
-- cuenta_pagar, en lugar de sumar o restar sobre el saldo de la cuenta de su factura.
-- Ver NOTA_CREDITO_DEBITO_PLAN.md seccion 4.
--
-- La fila de la nota lleva el id_fact_comp_cab de la factura que corrige, asi que la PK compuesta
-- (id_cta_pagar, id_fact_comp_cab) y las FKs de provision, orden de pago y rendicion no cambian.
-- La NC va con monto y saldo negativos ('Saldo a favor') y la ND positivos ('Pendiente'), con el
-- mismo importe que su fila de libro_iva_compra.
--
-- Correr los pasos en orden. Acordarse de cambiarlo tambien en el Power Architect.

-- 1. Columnas y FKs. El DEFAULT deja como FACTURA a las filas que ya existen.
ALTER TABLE public.cuenta_pagar ADD COLUMN cta_pag_origen VARCHAR(10) DEFAULT 'FACTURA' NOT NULL;
ALTER TABLE public.cuenta_pagar ADD COLUMN id_nota_cred_comp_cab INTEGER;
ALTER TABLE public.cuenta_pagar ADD COLUMN id_nota_debi_comp_cab INTEGER;

ALTER TABLE public.cuenta_pagar
    ADD CONSTRAINT nota_credito_compra_cabecera_cuenta_pagar_fk FOREIGN KEY (id_nota_cred_comp_cab)
    REFERENCES public.nota_credito_compra_cabecera (id_nota_cred_comp_cab)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;

ALTER TABLE public.cuenta_pagar
    ADD CONSTRAINT nota_debito_compra_cabecera_cuenta_pagar_fk FOREIGN KEY (id_nota_debi_comp_cab)
    REFERENCES public.nota_debito_compra_cabecera (id_nota_debi_comp_cab)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;

-- 2. Control previo: notas vigentes cuya factura ya entro en una provision no anulada.
--    Hasta ahora la nota cambiaba el saldo de la factura, y ese saldo pudo haberse provisionado o
--    pagado ya. El paso 3 igual separa las filas (la suma de la factura y su nota da lo mismo que
--    antes), pero en estas facturas puede quedar la factura con saldo y la nota con su total, que
--    netean entre si. Revisarlas despues de migrar.
SELECT 'NC' AS tipo, nc.id_nota_cred_comp_cab AS id_nota, nc.nota_cred_comp_numero AS numero,
       nc.id_fact_comp_cab, p.id_provi_cta_pagar_cabecera, p.prov_cta_pag_estado
  FROM public.nota_credito_compra_cabecera nc
  JOIN public.provision_cuenta_pagar_detalle d ON d.id_fact_comp_cab = nc.id_fact_comp_cab
  JOIN public.provision_cuenta_pagar p ON p.id_provi_cta_pagar_cabecera = d.id_provi_cta_pagar_cabecera
 WHERE nc.nota_cred_comp_estado <> 'Anulado' AND p.prov_cta_pag_estado <> 'Anulado'
UNION ALL
SELECT 'ND', nd.id_nota_debi_comp_cab, nd.nota_debi_comp_numero,
       nd.id_fact_comp_cab, p.id_provi_cta_pagar_cabecera, p.prov_cta_pag_estado
  FROM public.nota_debito_compra_cabecera nd
  JOIN public.provision_cuenta_pagar_detalle d ON d.id_fact_comp_cab = nd.id_fact_comp_cab
  JOIN public.provision_cuenta_pagar p ON p.id_provi_cta_pagar_cabecera = d.id_provi_cta_pagar_cabecera
 WHERE nd.nota_debi_comp_estado <> 'Anulado' AND p.prov_cta_pag_estado <> 'Anulado';

-- 3. Separacion de las notas vigentes. Las anuladas no llevan fila: su efecto ya estaba revertido.
BEGIN;

-- 3a. La fila de la factura recupera el saldo que le habian movido sus notas vigentes. El total del
--     libro IVA ya tiene el signo de la nota (NC negativo, ND positivo), asi que se resta tal cual.
UPDATE public.cuenta_pagar cp
   SET cta_pag_saldo = cp.cta_pag_saldo - n.total
  FROM (SELECT l.id_fact_comp_cab, SUM(l.libro_iva_comp_total) AS total
          FROM public.libro_iva_compra l
          LEFT JOIN public.nota_credito_compra_cabecera nc ON nc.id_nota_cred_comp_cab = l.id_nota_cred_comp_cab
          LEFT JOIN public.nota_debito_compra_cabecera nd ON nd.id_nota_debi_comp_cab = l.id_nota_debi_comp_cab
         WHERE l.libro_iva_comp_origen IN ('NOTA_CRED', 'NOTA_DEBI')
           AND COALESCE(l.libro_iva_comp_estado, '') <> 'Anulado'
           AND COALESCE(nc.nota_cred_comp_estado, nd.nota_debi_comp_estado) <> 'Anulado'
         GROUP BY l.id_fact_comp_cab) n
 WHERE cp.id_fact_comp_cab = n.id_fact_comp_cab
   AND cp.cta_pag_origen = 'FACTURA'
   AND cp.cta_pag_estado <> 'Anulado';

-- 3b. Estado de la factura segun el nuevo saldo. Las que estan en provision o rendidas siguen asi.
UPDATE public.cuenta_pagar
   SET cta_pag_estado = CASE WHEN cta_pag_saldo > 0 THEN 'Pendiente'
                             WHEN cta_pag_saldo = 0 THEN 'Cancelado'
                             ELSE 'Saldo a favor' END
 WHERE cta_pag_origen = 'FACTURA'
   AND cta_pag_estado IN ('Pendiente', 'Cancelado', 'Saldo a favor');

-- 3c. Una fila por nota vigente, con el importe de su fila de libro IVA. Vence en la emision.
INSERT INTO public.cuenta_pagar (id_fact_comp_cab, cta_pag_monto, cta_pag_estado, cta_pag_fecha_venci,
                                 cta_pag_saldo, cta_pag_plazo, cta_pag_origen, id_nota_cred_comp_cab)
SELECT nc.id_fact_comp_cab, l.libro_iva_comp_total, 'Saldo a favor', nc.nota_cred_comp_fecha_emision,
       l.libro_iva_comp_total, NULL, 'NOTA_CRED', nc.id_nota_cred_comp_cab
  FROM public.nota_credito_compra_cabecera nc
  JOIN public.libro_iva_compra l ON l.id_nota_cred_comp_cab = nc.id_nota_cred_comp_cab
 WHERE nc.nota_cred_comp_estado <> 'Anulado'
   AND COALESCE(l.libro_iva_comp_estado, '') <> 'Anulado';

INSERT INTO public.cuenta_pagar (id_fact_comp_cab, cta_pag_monto, cta_pag_estado, cta_pag_fecha_venci,
                                 cta_pag_saldo, cta_pag_plazo, cta_pag_origen, id_nota_debi_comp_cab)
SELECT nd.id_fact_comp_cab, l.libro_iva_comp_total, 'Pendiente', nd.nota_debi_comp_fecha_emision,
       l.libro_iva_comp_total, NULL, 'NOTA_DEBI', nd.id_nota_debi_comp_cab
  FROM public.nota_debito_compra_cabecera nd
  JOIN public.libro_iva_compra l ON l.id_nota_debi_comp_cab = nd.id_nota_debi_comp_cab
 WHERE nd.nota_debi_comp_estado <> 'Anulado'
   AND COALESCE(l.libro_iva_comp_estado, '') <> 'Anulado';

COMMIT;

-- 4. Control: cada nota vigente con su fila, y el mismo importe que en el libro IVA.
-- SELECT cp.cta_pag_origen, cp.id_fact_comp_cab, COALESCE(cp.id_nota_cred_comp_cab, cp.id_nota_debi_comp_cab) AS id_nota,
--        cp.cta_pag_monto, cp.cta_pag_saldo, cp.cta_pag_estado
--   FROM public.cuenta_pagar cp
--  WHERE cp.cta_pag_origen <> 'FACTURA'
--  ORDER BY cp.id_fact_comp_cab, cp.id_cta_pagar;
