--Regenerá la BD desde Power Architect, y corré en este orden:
--esquema (Power Architect)  →  Procedimientos y Triggers para BD.sql  →  Inserts inciales.sql
--El orden importa: al insertar los factura_compra_detalle, el trigger de stock ya 
--debe existir para que stock se popule solo (art 1 y art 8 en depósito 1; art 6 en depósito 2).

-- INSERT TIPO ARTICULO
INSERT INTO public.tipo_articulo
(tipo_art_descripcion)
VALUES('Gaseosa');
INSERT INTO public.tipo_articulo
(tipo_art_descripcion)
VALUES('Cerveza');
INSERT INTO public.tipo_articulo
(tipo_art_descripcion)
VALUES('Jugo');
INSERT INTO public.tipo_articulo
(tipo_art_descripcion)
VALUES('Caña');
INSERT INTO public.tipo_articulo
(tipo_art_descripcion)
VALUES('Licor');
INSERT INTO public.tipo_articulo
(tipo_art_descripcion)
VALUES('Cigarrillo');
INSERT INTO public.tipo_articulo
(tipo_art_descripcion)
VALUES('Gaseosa');
INSERT INTO public.tipo_articulo
(tipo_art_descripcion)
VALUES('Cerveza');
INSERT INTO public.tipo_articulo
(tipo_art_descripcion)
VALUES('Jugo');
INSERT INTO public.tipo_articulo
(tipo_art_descripcion)
VALUES('Caña');
INSERT INTO public.tipo_articulo
(tipo_art_descripcion)
VALUES('Licor');


-- INSERT MARCA
INSERT INTO public.marca
(mar_descripcion)
VALUES('Coca Cola');
INSERT INTO public.marca
(mar_descripcion)
VALUES('Pilsen');
INSERT INTO public.marca
(mar_descripcion)
VALUES('Ouro Fino');
INSERT INTO public.marca
(mar_descripcion)
VALUES('Munic');
INSERT INTO public.marca
(mar_descripcion)
VALUES('Frugos');
INSERT INTO public.marca
(mar_descripcion)
VALUES('Del Valle');
INSERT INTO public.marca
(mar_descripcion)
VALUES('Tres Leones');
INSERT INTO public.marca
(mar_descripcion)
VALUES('Smirnoff');
INSERT INTO public.marca
(mar_descripcion)
VALUES('Palermo');
INSERT INTO public.marca
(mar_descripcion)
VALUES('San Marino');


-- INSERT IMPUESTO
INSERT INTO public.impuesto
(imp_descripcion)
VALUES('5');
INSERT INTO public.impuesto
(imp_descripcion)
VALUES('10');
INSERT INTO public.impuesto
(imp_descripcion)
VALUES('exentas');


-- INSERT PRESENTACION
INSERT INTO public.presentacion
(pres_descripcion)
VALUES('Botella de vidrio');
INSERT INTO public.presentacion
(pres_descripcion)
VALUES('Botella de plastico');
INSERT INTO public.presentacion
(pres_descripcion)
VALUES('Envase de carton');
INSERT INTO public.presentacion
(pres_descripcion)
VALUES('Lata');
INSERT INTO public.presentacion
(pres_descripcion)
VALUES('Caja dura');
INSERT INTO public.presentacion
(pres_descripcion)
VALUES('Caja blanda');


-- INSERTS PARA ARTICULOS
INSERT INTO public.articulo
(id_tipo_articulo, id_marca, id_impuesto, id_presentacion, art_descripcion, art_precio_compra, art_precio_venta, art_estado)
VALUES(1, 1, 1, 2, 'Coca Cola de 2 litros', 8000, 12000, 'activo');
INSERT INTO public.articulo
(id_tipo_articulo, id_marca, id_impuesto, id_presentacion, art_descripcion, art_precio_compra, art_precio_venta, art_estado)
VALUES(5, 8, 1, 1, 'Granadina botella de 1 litro', 12000, 18000, 'activo');
INSERT INTO public.articulo
(id_tipo_articulo, id_marca, id_impuesto, id_presentacion, art_descripcion, art_precio_compra, art_precio_venta, art_estado)
VALUES(3, 5, 1, 3, 'Jugo frugos de 1 litro', 7000, 10000, 'activo');
INSERT INTO public.articulo
(id_tipo_articulo, id_marca, id_impuesto, id_presentacion, art_descripcion, art_precio_compra, art_precio_venta, art_estado)
VALUES(4, 7, 1, 2, 'Tres leones de 500 ml', 2000, 5000, 'activo');
INSERT INTO public.articulo
(id_tipo_articulo, id_marca, id_impuesto, id_presentacion, art_descripcion, art_precio_compra, art_precio_venta, art_estado)
VALUES(2, 3, 1, 4, 'Ouro fino tubito en lata', 3000, 4000, 'activo');
INSERT INTO public.articulo
(id_tipo_articulo, id_marca, id_impuesto, id_presentacion, art_descripcion, art_precio_compra, art_precio_venta, art_estado)
VALUES(6, 9, 1, 5, 'Palermo Duo de 20', 2500, 4000, 'activo');
INSERT INTO public.articulo
(id_tipo_articulo, id_marca, id_impuesto, id_presentacion, art_descripcion, art_precio_compra, art_precio_venta, art_estado)
VALUES(6, 10, 1, 6, 'San marino de 10', 1800, 3000, 'activo');


-- INSERT SUCURSALES
INSERT INTO public.sucursal
(suc_descripcion, suc_direccion, suc_estado)
VALUES('Ciudad del Este - Don Bosco', 'Paraguay', 'activo');
INSERT INTO public.sucursal
(suc_descripcion, suc_direccion, suc_estado)
VALUES('Asuncion - Sajonia', 'Isabel la Catolica 1810', 'activo');
INSERT INTO public.sucursal
(suc_descripcion, suc_direccion, suc_estado)
VALUES('Villa Elisa - Tres Bocas', 'Americo Picco', 'activo');

-- INSERT DEPOSITOS
INSERT INTO public.deposito
(dep_descripcion, dep_estado, id_sucursal)
VALUES('Deposito1-Asu/Sajonia', 'activo', 2);
INSERT INTO public.deposito
(dep_descripcion, dep_estado, id_sucursal)
VALUES('Deposito2-Asu/Sajonia', 'activo', 2);
INSERT INTO public.deposito
(dep_descripcion, dep_estado, id_sucursal)
VALUES('Deposito1-CDE/DonBosco', 'activo', 1);


-- INSERT GRUPO
INSERT INTO public.grupo
(gru_descripcion)
VALUES('Administradores');
INSERT INTO public.grupo
(gru_descripcion)
VALUES('Tesoreria');
INSERT INTO public.grupo
(gru_descripcion)
VALUES('Ventas');
INSERT INTO public.grupo
(gru_descripcion)
VALUES('Compras');


-- INSERT MODULO
INSERT INTO public.modulo
(modu_descripcion)
VALUES('compra');
INSERT INTO public.modulo
(modu_descripcion)
VALUES('venta');
INSERT INTO public.modulo
(modu_descripcion)
VALUES('tesoreria');


-- INSERT PERMISO
INSERT INTO public.permiso
(id_grupo, id_modulo, permi_leer, permi_insertar, permi_borrar, permi_editar)
VALUES(1, 1, true, true, true, true);
INSERT INTO public.permiso
(id_grupo, id_modulo, permi_leer, permi_insertar, permi_borrar, permi_editar)
VALUES(1, 2, true, true, true, true);
INSERT INTO public.permiso
(id_grupo, id_modulo, permi_leer, permi_insertar, permi_borrar, permi_editar)
VALUES(1, 3, true, true, true, true);
INSERT INTO public.permiso
(id_grupo, id_modulo, permi_leer, permi_insertar, permi_borrar, permi_editar)
VALUES(2, 1, true, true, true, true);
INSERT INTO public.permiso
(id_grupo, id_modulo, permi_leer, permi_insertar, permi_borrar, permi_editar)
VALUES(2, 2, true, false, false, false);
INSERT INTO public.permiso
(id_grupo, id_modulo, permi_leer, permi_insertar, permi_borrar, permi_editar)
VALUES(2, 3, true, false, false, false);


--INSERT PERSONA
INSERT INTO public.persona
(per_nombre, per_apellido, per_ci, per_telefono, per_email, per_direccion, per_fecha_nac)
VALUES('Miguel', 'Gonzalez', '4856551', '0984334170', 'miguelg94@hotmail.com', 'Asuncion barrio Sajonia', '1994-12-10');
INSERT INTO public.persona
(per_nombre, per_apellido, per_ci, per_telefono, per_email, per_direccion, per_fecha_nac)
VALUES('Gustavo', 'Gonzalez', '4856574', '0987654321', 'gustatvo92@hotmail.com', 'Asuncion Sajonia', '1992-11-09');
INSERT INTO public.persona
(per_nombre, per_apellido, per_ci, per_telefono, per_email, per_direccion, per_fecha_nac)
VALUES('Adolfo Gustavo', 'Gonzalez', '123456', '0987123456', 'popini@hotmail.com', 'Asuncion Sajonia', '1963-05-02');
INSERT INTO public.persona
(per_nombre, per_apellido, per_ci, per_telefono, per_email, per_direccion, per_fecha_nac)
VALUES('Maria Stella', 'Gamarra', '1881070', '0982185292', 'mastella@hotmail.com', 'Ciudad del Este', '1972-02-03');


-- INSERT TIPO ENTIDAD
INSERT INTO public.tipo_entidad
(tipo_per_descripcion)
VALUES('fisica');
INSERT INTO public.tipo_entidad
(tipo_per_descripcion)
VALUES('juridica');


-- INSERT PROVEEDOR
INSERT INTO public.proveedor
(prov_razon_social, id_persona, id_tipo_entidad, prov_ruc, prov_nombre_comercial, prov_direccion, prov_telefono)
VALUES('Winner Comercial SRL', NULL, 2, '80010090-5', 'Winner Comercial', NULL, NULL);
INSERT INTO public.proveedor
(prov_razon_social, id_persona, id_tipo_entidad, prov_ruc, prov_nombre_comercial, prov_direccion, prov_telefono)
VALUES('Bruno Sebastian SA', NULL, 2, '123456-7', 'Comercial Bruno Sebastian', NULL, NULL);
INSERT INTO public.proveedor
(prov_razon_social, id_persona, id_tipo_entidad, prov_ruc, prov_nombre_comercial, prov_direccion, prov_telefono)
VALUES('Venlo', NULL, 2, '765432-1', 'Distribuidora Venlo', NULL, NULL);
INSERT INTO public.proveedor
(prov_razon_social, id_persona, id_tipo_entidad, prov_ruc, prov_nombre_comercial, prov_direccion, prov_telefono)
VALUES('Paresa', NULL, 2, '1111111-1', 'Paresa Coca Cola', NULL, NULL);
INSERT INTO public.proveedor
(prov_razon_social, id_persona, id_tipo_entidad, prov_ruc, prov_nombre_comercial, prov_direccion, prov_telefono)
VALUES('Refrescos del Paraguay', NULL, 2, '222222-2', 'Pepsico', NULL, NULL);
INSERT INTO public.proveedor
(prov_razon_social, id_persona, id_tipo_entidad, prov_ruc, prov_nombre_comercial, prov_direccion, prov_telefono)
VALUES('Emcesa', NULL, 2, '333333-3', 'Emcesa ', NULL, NULL);
INSERT INTO public.proveedor
(prov_razon_social, id_persona, id_tipo_entidad, prov_ruc, prov_nombre_comercial, prov_direccion, prov_telefono)
VALUES('Codisa', NULL, 2, '444444-4', 'Codisa', NULL, NULL);
INSERT INTO public.proveedor
(prov_razon_social, id_persona, id_tipo_entidad, prov_ruc, prov_nombre_comercial, prov_direccion, prov_telefono)
VALUES('Palermo SA', NULL, 2, '555555-5', 'Palermo', NULL, NULL);


-- INSERT USUARIO
INSERT INTO public.usuario
(id_persona, usu_user, usu_pass, usu_estado, id_grupo)
VALUES(1, 'admin', '123', 'activo', 1);
INSERT INTO public.usuario
(id_persona, usu_user, usu_pass, usu_estado, id_grupo)
VALUES(4, 'Maria Stella', '123', 'inactivo', 3);
INSERT INTO public.usuario
(id_persona, usu_user, usu_pass, usu_estado, id_grupo)
VALUES(2, 'Gustavo', '123', 'activo', 2);
INSERT INTO public.usuario
(id_persona, usu_user, usu_pass, usu_estado, id_grupo)
VALUES(3, 'Adolfo Gustavo', '123', 'activo', 4);






-- =====================================================================================
-- DATOS TRANSACCIONALES PARA PROBAR NOTA DE CREDITO / DEBITO DE COMPRA
-- Orden de ejecucion: esquema -> triggers -> este archivo (los factura_compra_detalle
-- disparan el trigger de stock, que puebla 'stock' solo).
-- =====================================================================================

-- INSERT TIPO COMPROBANTE
INSERT INTO public.tipo_comprobante (tipo_comprob_descripcion) VALUES('Factura');
INSERT INTO public.tipo_comprobante (tipo_comprob_descripcion) VALUES('Nota de Credito');
INSERT INTO public.tipo_comprobante (tipo_comprob_descripcion) VALUES('Nota de Debito');

-- INSERT TIMBRADO (vigente, para el tipo comprobante Factura)
INSERT INTO public.timbrado
(tim_numero, tim_fecha_autorizacion, tim_fecha_vencimineto, tim_estado, id_tipo_comprob)
VALUES(12345678, '2026-01-01', '2027-12-31', 'activo', 1);

-- ARTICULO ADICIONAL CON IVA 10% (los existentes son todos 5%) -> id 8
INSERT INTO public.articulo
(id_tipo_articulo, id_marca, id_impuesto, id_presentacion, art_descripcion, art_precio_compra, art_precio_venta, art_estado)
VALUES(2, 2, 2, 4, 'Pilsen lata 473ml', 5000, 7000, 'activo');

-- FACTURA DE COMPRA 1 (Credito, mercaderia) -> id 1. Proveedor 4 (Paresa), Sucursal 1, Usuario 1. Total 180000
INSERT INTO public.factura_compra_cabecera
(fact_comp_numero, fact_comp_timbrado, fact_comp_fecha_venci_timb, fact_comp_fecha_emision,
 fact_comp_fecha_carga, fact_comp_condicion, fact_comp_plazo, fact_comp_fecha_venci,
 fact_comp_observacion, fact_comp_estado, fact_comp_tipo_factura, id_proveedor, id_sucursal,
 id_usuario, id_orden_compra_cab)
VALUES('001-001-0000001', 12345678, '2027-12-31', '2026-07-01', '2026-07-01', 'Credito', 30,
       '2026-07-31', NULL, 'Pendiente', 'compraArt', 4, 2, 1, NULL);
INSERT INTO public.factura_compra_detalle
(id_fact_comp_cab, id_articulo, id_impuesto, id_deposito, fact_comp_cantidad, fact_comp_precio_compra, fact_det_descripcion)
VALUES(1, 1, 1, 1, 10, 8000, NULL);
INSERT INTO public.factura_compra_detalle
(id_fact_comp_cab, id_articulo, id_impuesto, id_deposito, fact_comp_cantidad, fact_comp_precio_compra, fact_det_descripcion)
VALUES(1, 8, 2, 1, 20, 5000, NULL);
INSERT INTO public.cuenta_pagar
(id_fact_comp_cab, cta_pag_monto, cta_pag_estado, cta_pag_fecha_venci, cta_pag_saldo, cta_pag_plazo)
VALUES(1, 180000, 'Pendiente', '2026-07-31', 180000, 30);
INSERT INTO public.libro_iva_compra
(id_fact_comp_cab, id_nota_cred_comp_cab, id_nota_debi_comp_cab, libro_iva_comp_fecha,
 libro_iva_comp_5, libro_iva_comp_10, libro_iva_comp_gravada_10, libro_iva_comp_gravada_5,
 libro_iva_comp_exenta, libro_iva_comp_total, libro_iva_comp_estado, libro_iva_comp_origen)
VALUES(1, NULL, NULL, '2026-07-01', 3809, 9090, 90910, 76191, 0, 180000, 'Activo', 'FACTURA');

-- FACTURA DE COMPRA 2 (Contado, mercaderia) -> id 2. Proveedor 8 (Palermo), Sucursal 1, Usuario 1. Total 125000
INSERT INTO public.factura_compra_cabecera
(fact_comp_numero, fact_comp_timbrado, fact_comp_fecha_venci_timb, fact_comp_fecha_emision,
 fact_comp_fecha_carga, fact_comp_condicion, fact_comp_plazo, fact_comp_fecha_venci,
 fact_comp_observacion, fact_comp_estado, fact_comp_tipo_factura, id_proveedor, id_sucursal,
 id_usuario, id_orden_compra_cab)
VALUES('001-001-0000002', 12345678, '2027-12-31', '2026-07-02', '2026-07-02', 'Contado', NULL,
       '2026-07-02', NULL, 'Pendiente', 'compraArt', 8, 2, 1, NULL);
INSERT INTO public.factura_compra_detalle
(id_fact_comp_cab, id_articulo, id_impuesto, id_deposito, fact_comp_cantidad, fact_comp_precio_compra, fact_det_descripcion)
VALUES(2, 6, 1, 2, 50, 2500, NULL);
INSERT INTO public.cuenta_pagar
(id_fact_comp_cab, cta_pag_monto, cta_pag_estado, cta_pag_fecha_venci, cta_pag_saldo, cta_pag_plazo)
VALUES(2, 125000, 'Pendiente', '2026-07-02', 125000, NULL);
INSERT INTO public.libro_iva_compra
(id_fact_comp_cab, id_nota_cred_comp_cab, id_nota_debi_comp_cab, libro_iva_comp_fecha,
 libro_iva_comp_5, libro_iva_comp_10, libro_iva_comp_gravada_10, libro_iva_comp_gravada_5,
 libro_iva_comp_exenta, libro_iva_comp_total, libro_iva_comp_estado, libro_iva_comp_origen)
VALUES(2, NULL, NULL, '2026-07-02', 5952, 0, 0, 119048, 0, 125000, 'Activo', 'FACTURA');
