-- Migración de tipos: los importes de INTEGER a BIGINT y los nros de comprobante a texto.
--
-- Motivo: el techo de INTEGER es 2.147.483.647, o sea unos 2.147 millones de guaraníes. Un saldo
-- bancario o un acumulado lo pasa sin esfuerzo — se cayó al cargar un crédito el 2026-09-05 con
-- 'el entero está fuera de rango'. Estaba anotado como riesgo desde el 2026-08-17 en
-- MODULO_TESORERIA_PLAN.md §8.
--
-- No requiere ningún cambio en Java: los importes ya son Long en los POJOs y los DAOs los leen y
-- escriben con getLong/setLong. Es una migración sólo de base de datos.
--
-- Quedan afuera a propósito los INTEGER que no son importes: números de documento, de cheque, de
-- comprobante, cantidades, timbrados y plazos.
--
-- Acordarse de cambiar también el tipo en el Power Architect, para que el modelo no quede atrás.

BEGIN;

-- apertura_cierre_caja
ALTER TABLE public.apertura_cierre_caja ALTER COLUMN aper_cier_cheque TYPE BIGINT;
ALTER TABLE public.apertura_cierre_caja ALTER COLUMN aper_cier_efectivo TYPE BIGINT;
ALTER TABLE public.apertura_cierre_caja ALTER COLUMN aper_cier_monto_cierre TYPE BIGINT;
ALTER TABLE public.apertura_cierre_caja ALTER COLUMN aper_cier_monto_inicial TYPE BIGINT;
ALTER TABLE public.apertura_cierre_caja ALTER COLUMN aper_cier_tarjeta TYPE BIGINT;
-- arqueo_caja
ALTER TABLE public.arqueo_caja ALTER COLUMN arque_caja_cheque TYPE BIGINT;
ALTER TABLE public.arqueo_caja ALTER COLUMN arque_caja_efectivo TYPE BIGINT;
ALTER TABLE public.arqueo_caja ALTER COLUMN arque_caja_tarjeta TYPE BIGINT;
-- articulo
ALTER TABLE public.articulo ALTER COLUMN art_precio_compra TYPE BIGINT;
ALTER TABLE public.articulo ALTER COLUMN art_precio_venta TYPE BIGINT;
-- cobro
ALTER TABLE public.cobro ALTER COLUMN cob_det_monto TYPE BIGINT;
-- conciliacion_bancaria
ALTER TABLE public.conciliacion_bancaria ALTER COLUMN conc_banc_saldo_banco TYPE BIGINT;
ALTER TABLE public.conciliacion_bancaria ALTER COLUMN conc_bancaria_saldo_final TYPE BIGINT;
ALTER TABLE public.conciliacion_bancaria ALTER COLUMN conc_bancaria_saldo_inicial TYPE BIGINT;
-- conciliacion_bancaria_detalle
ALTER TABLE public.conciliacion_bancaria_detalle ALTER COLUMN conc_bancaria_monto TYPE BIGINT;
-- creditos
ALTER TABLE public.creditos ALTER COLUMN credito_monto TYPE BIGINT;
-- cuenta_cobrar
ALTER TABLE public.cuenta_cobrar ALTER COLUMN cta_cob_monto TYPE BIGINT;
ALTER TABLE public.cuenta_cobrar ALTER COLUMN cta_cob_saldo TYPE BIGINT;
-- cuenta_pagar
ALTER TABLE public.cuenta_pagar ALTER COLUMN cta_pag_monto TYPE BIGINT;
ALTER TABLE public.cuenta_pagar ALTER COLUMN cta_pag_saldo TYPE BIGINT;
-- debitos
ALTER TABLE public.debitos ALTER COLUMN debito_monto TYPE BIGINT;
-- factura_compra_detalle
ALTER TABLE public.factura_compra_detalle ALTER COLUMN fact_comp_precio_compra TYPE BIGINT;
-- factura_venta_detalle
ALTER TABLE public.factura_venta_detalle ALTER COLUMN fact_venta_precio_venta TYPE BIGINT;
-- fondo_fijo
ALTER TABLE public.fondo_fijo ALTER COLUMN fondo_fijo_monto_asignado TYPE BIGINT;
-- fondo_fijo_rendicion_detalle
ALTER TABLE public.fondo_fijo_rendicion_detalle ALTER COLUMN monto_rendido TYPE BIGINT;
-- forma_cobro_detalle
ALTER TABLE public.forma_cobro_detalle ALTER COLUMN forma_cob_cheque TYPE BIGINT;
ALTER TABLE public.forma_cobro_detalle ALTER COLUMN forma_cob_efectivo TYPE BIGINT;
ALTER TABLE public.forma_cobro_detalle ALTER COLUMN forma_cob_tarjeta TYPE BIGINT;
ALTER TABLE public.forma_cobro_detalle ALTER COLUMN forma_cob_total TYPE BIGINT;
-- forma_pago_detalle
ALTER TABLE public.forma_pago_detalle ALTER COLUMN forma_pag_monto TYPE BIGINT;
-- libro_iva_compra
ALTER TABLE public.libro_iva_compra ALTER COLUMN libro_iva_comp_10 TYPE BIGINT;
ALTER TABLE public.libro_iva_compra ALTER COLUMN libro_iva_comp_5 TYPE BIGINT;
ALTER TABLE public.libro_iva_compra ALTER COLUMN libro_iva_comp_exenta TYPE BIGINT;
ALTER TABLE public.libro_iva_compra ALTER COLUMN libro_iva_comp_gravada_10 TYPE BIGINT;
ALTER TABLE public.libro_iva_compra ALTER COLUMN libro_iva_comp_gravada_5 TYPE BIGINT;
ALTER TABLE public.libro_iva_compra ALTER COLUMN libro_iva_comp_total TYPE BIGINT;
-- nota_credito_compra_detalle
ALTER TABLE public.nota_credito_compra_detalle ALTER COLUMN nota_cred_monto TYPE BIGINT;
-- nota_debito_compra_detalle
ALTER TABLE public.nota_debito_compra_detalle ALTER COLUMN nota_debi_monto TYPE BIGINT;
-- nota_debito_venta_detalle
ALTER TABLE public.nota_debito_venta_detalle ALTER COLUMN nota_debi_vent_monto TYPE BIGINT;
-- orden_compra_detalle
ALTER TABLE public.orden_compra_detalle ALTER COLUMN orden_compr_det_precio_compra TYPE BIGINT;
-- orden_pago_cabecera
ALTER TABLE public.orden_pago_cabecera ALTER COLUMN ord_pag_monto TYPE BIGINT;
-- orden_pago_detalle
ALTER TABLE public.orden_pago_detalle ALTER COLUMN orden_pag_det_monto TYPE BIGINT;
-- presupuesto_detalle
ALTER TABLE public.presupuesto_detalle ALTER COLUMN presu_det_precio_compra TYPE BIGINT;
-- provision_cuenta_pagar_detalle
ALTER TABLE public.provision_cuenta_pagar_detalle ALTER COLUMN prov_cta_pag_monto TYPE BIGINT;
-- recaudaciones_depositar_detalle
ALTER TABLE public.recaudaciones_depositar_detalle ALTER COLUMN rec_depositar_cheque TYPE BIGINT;
ALTER TABLE public.recaudaciones_depositar_detalle ALTER COLUMN rec_depositar_efectivo TYPE BIGINT;

-- Nros de comprobante bancario a texto: los da el banco y pueden traer letras, guiones y ceros a
-- la izquierda, ademas de no entrar en un INTEGER. Mismo criterio que fact_comp_numero, que ya es
-- VARCHAR, y que forma_pag_referencia ('Nro cheque, transferencia, etc').
ALTER TABLE public.debitos  ALTER COLUMN debitos_nro_comprobante  TYPE VARCHAR(30) USING debitos_nro_comprobante::VARCHAR;
ALTER TABLE public.creditos ALTER COLUMN creditos_nro_comprobante TYPE VARCHAR(30) USING creditos_nro_comprobante::VARCHAR;

COMMIT;
