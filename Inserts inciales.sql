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
VALUES('Deposito1-Asu/Sajonia', 'activo', 1);
INSERT INTO public.deposito
(dep_descripcion, dep_estado, id_sucursal)
VALUES('Deposito2-Asu/Sajonia', 'activo', 1);
INSERT INTO public.deposito
(dep_descripcion, dep_estado, id_sucursal)
VALUES('Deposito1-CDE/DonBosco', 'activo', 3);


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
VALUES(3, 'Gustavo', '123', 'activo', 2);
INSERT INTO public.usuario
(id_persona, usu_user, usu_pass, usu_estado, id_grupo)
VALUES(5, 'Adolfo Gustavo', '123', 'activo', 4);





