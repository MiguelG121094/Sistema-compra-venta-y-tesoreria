
CREATE SEQUENCE public.tipo_tarjeta_id_tipo_tarjeta_seq;

CREATE TABLE public.tipo_tarjeta (
                id_tipo_tarjeta INTEGER NOT NULL DEFAULT nextval('public.tipo_tarjeta_id_tipo_tarjeta_seq'),
                tipo_tarjeta_descripcion VARCHAR(50) NOT NULL,
                CONSTRAINT id_tipo_tarjeta PRIMARY KEY (id_tipo_tarjeta)
);
COMMENT ON TABLE public.tipo_tarjeta IS 'debito, credito';


ALTER SEQUENCE public.tipo_tarjeta_id_tipo_tarjeta_seq OWNED BY public.tipo_tarjeta.id_tipo_tarjeta;

CREATE SEQUENCE public.tipo_cuenta_id_tipo_cuenta_seq;

CREATE TABLE public.tipo_cuenta (
                id_tipo_cuenta INTEGER NOT NULL DEFAULT nextval('public.tipo_cuenta_id_tipo_cuenta_seq'),
                tipo_cuenta_descripcion VARCHAR(50) NOT NULL,
                CONSTRAINT id_tipo_cuenta PRIMARY KEY (id_tipo_cuenta)
);
COMMENT ON TABLE public.tipo_cuenta IS 'cuenta corriente, cuenta de ahorro, etc.';


ALTER SEQUENCE public.tipo_cuenta_id_tipo_cuenta_seq OWNED BY public.tipo_cuenta.id_tipo_cuenta;

CREATE SEQUENCE public.tipo_entidad_financiera_id_tipo_enti_finan_seq;

CREATE TABLE public.tipo_entidad_financiera (
                id_tipo_enti_finan INTEGER NOT NULL DEFAULT nextval('public.tipo_entidad_financiera_id_tipo_enti_finan_seq'),
                tipo_enti_finan_descripcion VARCHAR(50) NOT NULL,
                CONSTRAINT id_tipo_enti_finan PRIMARY KEY (id_tipo_enti_finan)
);
COMMENT ON TABLE public.tipo_entidad_financiera IS 'banco, financiera, cooperativa, etc.';


ALTER SEQUENCE public.tipo_entidad_financiera_id_tipo_enti_finan_seq OWNED BY public.tipo_entidad_financiera.id_tipo_enti_finan;

CREATE SEQUENCE public.entidad_financiera_id_enti_finan_seq;

CREATE TABLE public.entidad_financiera (
                id_enti_finan INTEGER NOT NULL DEFAULT nextval('public.entidad_financiera_id_enti_finan_seq'),
                enti_finan_nombre VARCHAR(50) NOT NULL,
                id_tipo_enti_finan INTEGER NOT NULL,
                CONSTRAINT id_enti_finan PRIMARY KEY (id_enti_finan)
);
COMMENT ON TABLE public.entidad_financiera IS 'itau, ueno, atlas, paraguayo japonesa, tu financiera, medalla, san cristobal, etc.';


ALTER SEQUENCE public.entidad_financiera_id_enti_finan_seq OWNED BY public.entidad_financiera.id_enti_finan;

CREATE SEQUENCE public.tarjeta_id_tarjeta_seq;

CREATE TABLE public.tarjeta (
                id_tarjeta INTEGER NOT NULL DEFAULT nextval('public.tarjeta_id_tarjeta_seq'),
                id_tipo_tarjeta INTEGER NOT NULL,
                id_enti_finan INTEGER NOT NULL,
                id_tipo_cuenta INTEGER NOT NULL,
                CONSTRAINT id_tarjeta PRIMARY KEY (id_tarjeta)
);


ALTER SEQUENCE public.tarjeta_id_tarjeta_seq OWNED BY public.tarjeta.id_tarjeta;

CREATE SEQUENCE public.moneda_id_moneda_seq;

CREATE TABLE public.moneda (
                id_moneda INTEGER NOT NULL DEFAULT nextval('public.moneda_id_moneda_seq'),
                moneda_descipcion VARCHAR(50) NOT NULL,
                CONSTRAINT id_moneda PRIMARY KEY (id_moneda)
);
COMMENT ON TABLE public.moneda IS 'Guaranies, dolar';


ALTER SEQUENCE public.moneda_id_moneda_seq OWNED BY public.moneda.id_moneda;

CREATE SEQUENCE public.cuenta_id_cuenta_seq;

CREATE TABLE public.cuenta (
                id_cuenta INTEGER NOT NULL DEFAULT nextval('public.cuenta_id_cuenta_seq'),
                id_tipo_cuenta INTEGER NOT NULL,
                id_enti_finan INTEGER NOT NULL,
                cuenta_numero INTEGER NOT NULL,
                id_moneda INTEGER NOT NULL,
                CONSTRAINT id_cuenta PRIMARY KEY (id_cuenta)
);
COMMENT ON COLUMN public.cuenta.id_tipo_cuenta IS 'cuenta corriente, caja de ahorro';


ALTER SEQUENCE public.cuenta_id_cuenta_seq OWNED BY public.cuenta.id_cuenta;

CREATE SEQUENCE public.conciliacion_bancaria_id_conc_bancaria_seq;

CREATE TABLE public.conciliacion_bancaria (
                id_conc_bancaria INTEGER NOT NULL DEFAULT nextval('public.conciliacion_bancaria_id_conc_bancaria_seq'),
                id_cuenta INTEGER NOT NULL,
                conc_bancaria_fecha_desde DATE NOT NULL,
                conc_bancaria_fecha DATE NOT NULL,
                conc_bancaria_fecha_hasta DATE NOT NULL,
                conc_bancaria_saldo_inicial INTEGER NOT NULL,
                conc_bancaria_saldo_final INTEGER NOT NULL,
                conc_banc_saldo_banco INTEGER NOT NULL,
                CONSTRAINT id_conc_bancaria PRIMARY KEY (id_conc_bancaria)
);
COMMENT ON TABLE public.conciliacion_bancaria IS 'cuando se guarda una orden de pago viene y guarda tambien en la conciliacion la orden de pago que se hizo en cheque o transferencia y esa conciliacion es de la cuenta de itau y pasa restando el monto que se pagó';


ALTER SEQUENCE public.conciliacion_bancaria_id_conc_bancaria_seq OWNED BY public.conciliacion_bancaria.id_conc_bancaria;

CREATE SEQUENCE public.debitos_id_debitos_seq;

CREATE TABLE public.debitos (
                id_debitos INTEGER NOT NULL DEFAULT nextval('public.debitos_id_debitos_seq'),
                debitos_nro_comprobante INTEGER NOT NULL,
                debitos_fecha DATE NOT NULL,
                debitos_detalle VARCHAR(255) NOT NULL,
                id_cuenta INTEGER NOT NULL,
                debito_monto INTEGER NOT NULL,
                CONSTRAINT id_debitos PRIMARY KEY (id_debitos)
);
COMMENT ON TABLE public.debitos IS 'comisiones bancarias, comisiones por cheques en dolars, gastos administrativos del banco';
COMMENT ON COLUMN public.debitos.debitos_detalle IS 'concepto de que se carga el debito, ejemplo descuento de comisiones bancaria, debito automatico de TC';


ALTER SEQUENCE public.debitos_id_debitos_seq OWNED BY public.debitos.id_debitos;

CREATE SEQUENCE public.chequera_id_chequera_seq;

CREATE TABLE public.chequera (
                id_chequera INTEGER NOT NULL DEFAULT nextval('public.chequera_id_chequera_seq'),
                id_cuenta INTEGER NOT NULL,
                chequera_serie INTEGER NOT NULL,
                chequera_desde_nro INTEGER NOT NULL,
                chequera_hasta_nro INTEGER NOT NULL,
                CONSTRAINT id_chequera PRIMARY KEY (id_chequera)
);


ALTER SEQUENCE public.chequera_id_chequera_seq OWNED BY public.chequera.id_chequera;

CREATE SEQUENCE public.tipo_cheque_id_tipo_cheque_seq;

CREATE TABLE public.tipo_cheque (
                id_tipo_cheque INTEGER NOT NULL DEFAULT nextval('public.tipo_cheque_id_tipo_cheque_seq'),
                tipo_cheque_descripcion VARCHAR(25) NOT NULL,
                CONSTRAINT id_tipo_cheque PRIMARY KEY (id_tipo_cheque)
);
COMMENT ON TABLE public.tipo_cheque IS 'diferido, a la vista';
COMMENT ON COLUMN public.tipo_cheque.tipo_cheque_descripcion IS '- Diferido: tiene dos fechas, la primera, la fecha de emisión del cheque y la segunda la fecha a partir de la cual se puede efectivizar el cheque. - A la orden: van específicamente a nombre de una persona. No se pueden endosar, solamente cobrar o depositar. - Al portador o No a la orden: cuando no se escribe el nombre de quien lo recibe, se pueden endosar, cobrar o depositar. - Cruzado: cuando se dibujan dos rayas diagonales en una de las esquinas del cheque. Esto indica que no puede cobrarse en ventanilla, solo debe depositarse en otra cuenta bancaria.';


ALTER SEQUENCE public.tipo_cheque_id_tipo_cheque_seq OWNED BY public.tipo_cheque.id_tipo_cheque;

CREATE SEQUENCE public.recaudaciones_depositar_id_rec_depositar_seq;

CREATE TABLE public.recaudaciones_depositar (
                id_rec_depositar INTEGER NOT NULL DEFAULT nextval('public.recaudaciones_depositar_id_rec_depositar_seq'),
                rec_depositar_fecha DATE NOT NULL,
                rec_depositar_estado VARCHAR(20) NOT NULL,
                rec_depositar_referencia VARCHAR,
                id_cuenta_destino INTEGER NOT NULL,
                CONSTRAINT id_rec_depositar PRIMARY KEY (id_rec_depositar)
);
COMMENT ON COLUMN public.recaudaciones_depositar.rec_depositar_referencia IS 'Nro boleta depósito';
COMMENT ON COLUMN public.recaudaciones_depositar.id_cuenta_destino IS 'Cuenta bancaria donde se deposito, analizar esto si se hace el registro del credito en la cuenta con la tabla crédito o se crea un credito con trigger al cargar el numero de boleta de deposito';


ALTER SEQUENCE public.recaudaciones_depositar_id_rec_depositar_seq OWNED BY public.recaudaciones_depositar.id_rec_depositar;

CREATE SEQUENCE public.tipo_comprobante_id_tipo_comprob_seq;

CREATE TABLE public.tipo_comprobante (
                id_tipo_comprob INTEGER NOT NULL DEFAULT nextval('public.tipo_comprobante_id_tipo_comprob_seq'),
                tipo_comprob_descripcion VARCHAR(50) NOT NULL,
                CONSTRAINT id_tipo_comprob PRIMARY KEY (id_tipo_comprob)
);
COMMENT ON TABLE public.tipo_comprobante IS 'factura, nota de remision, nota de credito, nota de debito,etc.';


ALTER SEQUENCE public.tipo_comprobante_id_tipo_comprob_seq OWNED BY public.tipo_comprobante.id_tipo_comprob;

CREATE SEQUENCE public.timbrado_id_timbrado_seq;

CREATE TABLE public.timbrado (
                id_timbrado INTEGER NOT NULL DEFAULT nextval('public.timbrado_id_timbrado_seq'),
                tim_numero INTEGER NOT NULL,
                tim_fecha_autorizacion DATE NOT NULL,
                tim_fecha_vencimineto DATE NOT NULL,
                tim_estado VARCHAR(20) NOT NULL,
                id_tipo_comprob INTEGER NOT NULL,
                CONSTRAINT id_timbrado PRIMARY KEY (id_timbrado)
);


ALTER SEQUENCE public.timbrado_id_timbrado_seq OWNED BY public.timbrado.id_timbrado;

CREATE SEQUENCE public.tipo_entidad_id_tipo_entidad_seq;

CREATE TABLE public.tipo_entidad (
                id_tipo_entidad INTEGER NOT NULL DEFAULT nextval('public.tipo_entidad_id_tipo_entidad_seq'),
                tipo_per_descripcion VARCHAR(25) NOT NULL,
                CONSTRAINT id_tipo_entidad PRIMARY KEY (id_tipo_entidad)
);
COMMENT ON TABLE public.tipo_entidad IS 'fisica, juridica';


ALTER SEQUENCE public.tipo_entidad_id_tipo_entidad_seq OWNED BY public.tipo_entidad.id_tipo_entidad;

CREATE SEQUENCE public.forma_cobro_cabecera_id_forma_cobro_cabecera_seq;

CREATE TABLE public.forma_cobro_cabecera (
                id_forma_cobro INTEGER NOT NULL DEFAULT nextval('public.forma_cobro_cabecera_id_forma_cobro_cabecera_seq'),
                for_cobro_descripcion VARCHAR(50) NOT NULL,
                for_cobro_estado VARCHAR(20) NOT NULL,
                CONSTRAINT id_forma_cobro PRIMARY KEY (id_forma_cobro)
);


ALTER SEQUENCE public.forma_cobro_cabecera_id_forma_cobro_cabecera_seq OWNED BY public.forma_cobro_cabecera.id_forma_cobro;

CREATE SEQUENCE public.motivo_ajuste_id_motivo_ajuste_seq;

CREATE TABLE public.motivo_ajuste (
                id_motivo_ajuste INTEGER NOT NULL DEFAULT nextval('public.motivo_ajuste_id_motivo_ajuste_seq'),
                mot_aju_descripcion VARCHAR(200) NOT NULL,
                mot_aju_estado VARCHAR(20) NOT NULL,
                CONSTRAINT id_motivo_ajuste PRIMARY KEY (id_motivo_ajuste)
);


ALTER SEQUENCE public.motivo_ajuste_id_motivo_ajuste_seq OWNED BY public.motivo_ajuste.id_motivo_ajuste;

CREATE SEQUENCE public.ajuste_stock_cabecera_id_ajuste_stock_cab_cabecera_seq;

CREATE TABLE public.ajuste_stock_cabecera (
                id_ajuste_stock_cab INTEGER NOT NULL DEFAULT nextval('public.ajuste_stock_cabecera_id_ajuste_stock_cab_cabecera_seq'),
                aju_stk_fecha DATE NOT NULL,
                aju_stk_observacion VARCHAR NOT NULL,
                CONSTRAINT id_ajuste_stock_cab PRIMARY KEY (id_ajuste_stock_cab)
);


ALTER SEQUENCE public.ajuste_stock_cabecera_id_ajuste_stock_cab_cabecera_seq OWNED BY public.ajuste_stock_cabecera.id_ajuste_stock_cab;

CREATE SEQUENCE public.forma_pago_cabecera_id_forma_pago_cab_cabecera_seq;

CREATE TABLE public.forma_pago_cabecera (
                id_forma_pago_cab INTEGER NOT NULL DEFAULT nextval('public.forma_pago_cabecera_id_forma_pago_cab_cabecera_seq'),
                forma_pago_descripcion VARCHAR(25) NOT NULL,
                CONSTRAINT id_forma_pago_cabecera PRIMARY KEY (id_forma_pago_cab)
);
COMMENT ON TABLE public.forma_pago_cabecera IS 'cheque o transferencia, en la orden de pago no se puede pagar con efectivo para eso está el fondo fijo que se repone con orden de pago, o en todo caso se saca un cheque y se efectivisa';


ALTER SEQUENCE public.forma_pago_cabecera_id_forma_pago_cab_cabecera_seq OWNED BY public.forma_pago_cabecera.id_forma_pago_cab;

CREATE SEQUENCE public.presentacion_id_presentacion_seq;

CREATE TABLE public.presentacion (
                id_presentacion INTEGER NOT NULL DEFAULT nextval('public.presentacion_id_presentacion_seq'),
                pres_descripcion VARCHAR(50) NOT NULL,
                CONSTRAINT id_presentacion PRIMARY KEY (id_presentacion)
);
COMMENT ON TABLE public.presentacion IS 'botella plastica, botella de vidrio, embase de plastico, embase de carton,etc.';


ALTER SEQUENCE public.presentacion_id_presentacion_seq OWNED BY public.presentacion.id_presentacion;

CREATE SEQUENCE public.sucursal_id_sucursal_seq;

CREATE TABLE public.sucursal (
                id_sucursal INTEGER NOT NULL DEFAULT nextval('public.sucursal_id_sucursal_seq'),
                suc_descripcion VARCHAR(50) NOT NULL,
                suc_direccion VARCHAR(200),
                suc_estado VARCHAR(20) NOT NULL,
                CONSTRAINT id_sucursal PRIMARY KEY (id_sucursal)
);


ALTER SEQUENCE public.sucursal_id_sucursal_seq OWNED BY public.sucursal.id_sucursal;

CREATE SEQUENCE public.caja_id_caja_seq;

CREATE TABLE public.caja (
                id_caja INTEGER NOT NULL DEFAULT nextval('public.caja_id_caja_seq'),
                caja_descripcion VARCHAR(50) NOT NULL,
                caja_nro_expedicion INTEGER NOT NULL,
                caja_estado VARCHAR(20) NOT NULL,
                id_sucursal INTEGER NOT NULL,
                CONSTRAINT id_caja PRIMARY KEY (id_caja)
);


ALTER SEQUENCE public.caja_id_caja_seq OWNED BY public.caja.id_caja;

CREATE SEQUENCE public.impuesto_id_impuesto_seq;

CREATE TABLE public.impuesto (
                id_impuesto INTEGER NOT NULL DEFAULT nextval('public.impuesto_id_impuesto_seq'),
                imp_descripcion VARCHAR(50) NOT NULL,
                CONSTRAINT id_impuesto PRIMARY KEY (id_impuesto)
);
COMMENT ON TABLE public.impuesto IS '5%, 10%, exentas.';


ALTER SEQUENCE public.impuesto_id_impuesto_seq OWNED BY public.impuesto.id_impuesto;

CREATE SEQUENCE public.grupo_id_grupo_seq;

CREATE TABLE public.grupo (
                id_grupo INTEGER NOT NULL DEFAULT nextval('public.grupo_id_grupo_seq'),
                gru_descripcion VARCHAR(50) NOT NULL,
                CONSTRAINT id_grupo PRIMARY KEY (id_grupo)
);
COMMENT ON TABLE public.grupo IS 'administrador, usuario';


ALTER SEQUENCE public.grupo_id_grupo_seq OWNED BY public.grupo.id_grupo;

CREATE SEQUENCE public.modulo_id_modulo_seq;

CREATE TABLE public.modulo (
                id_modulo INTEGER NOT NULL DEFAULT nextval('public.modulo_id_modulo_seq'),
                modu_descripcion VARCHAR(25) NOT NULL,
                CONSTRAINT id_modulo PRIMARY KEY (id_modulo)
);
COMMENT ON TABLE public.modulo IS 'compra, venta, tesoreria';


ALTER SEQUENCE public.modulo_id_modulo_seq OWNED BY public.modulo.id_modulo;

CREATE TABLE public.permiso (
                id_grupo INTEGER NOT NULL,
                id_modulo INTEGER NOT NULL,
                permi_leer BOOLEAN NOT NULL,
                permi_insertar BOOLEAN NOT NULL,
                permi_borrar BOOLEAN NOT NULL,
                permi_editar BOOLEAN NOT NULL,
                CONSTRAINT id_permiso PRIMARY KEY (id_grupo, id_modulo)
);


CREATE SEQUENCE public.marca_id_marca_seq;

CREATE TABLE public.marca (
                id_marca INTEGER NOT NULL DEFAULT nextval('public.marca_id_marca_seq'),
                mar_descripcion VARCHAR(50) NOT NULL,
                CONSTRAINT id_marca PRIMARY KEY (id_marca)
);


ALTER SEQUENCE public.marca_id_marca_seq OWNED BY public.marca.id_marca;

CREATE SEQUENCE public.deposito_id_deposito_seq;

CREATE TABLE public.deposito (
                id_deposito INTEGER NOT NULL DEFAULT nextval('public.deposito_id_deposito_seq'),
                dep_descripcion VARCHAR(100) NOT NULL,
                dep_estado VARCHAR(20) NOT NULL,
                id_sucursal INTEGER NOT NULL,
                CONSTRAINT id_deposito PRIMARY KEY (id_deposito)
);


ALTER SEQUENCE public.deposito_id_deposito_seq OWNED BY public.deposito.id_deposito;

CREATE SEQUENCE public.tipo_articulo_id_tipo_articulo_seq;

CREATE TABLE public.tipo_articulo (
                id_tipo_articulo INTEGER NOT NULL DEFAULT nextval('public.tipo_articulo_id_tipo_articulo_seq'),
                tipo_art_descripcion VARCHAR(50) NOT NULL,
                CONSTRAINT id_tipo_articulo PRIMARY KEY (id_tipo_articulo)
);
COMMENT ON TABLE public.tipo_articulo IS 'gaseosa, cerveza, jugo, caramelo, chicle, etc.';


ALTER SEQUENCE public.tipo_articulo_id_tipo_articulo_seq OWNED BY public.tipo_articulo.id_tipo_articulo;

CREATE SEQUENCE public.articulo_id_articulo_seq;

CREATE TABLE public.articulo (
                id_articulo INTEGER NOT NULL DEFAULT nextval('public.articulo_id_articulo_seq'),
                id_tipo_articulo INTEGER,
                id_marca INTEGER,
                id_impuesto INTEGER NOT NULL,
                id_presentacion INTEGER,
                art_descripcion VARCHAR(100) NOT NULL,
                art_precio_compra INTEGER,
                art_precio_venta INTEGER NOT NULL,
                art_estado VARCHAR(20) NOT NULL,
                CONSTRAINT id_articulo PRIMARY KEY (id_articulo)
);


ALTER SEQUENCE public.articulo_id_articulo_seq OWNED BY public.articulo.id_articulo;

CREATE TABLE public.ajuste_stock_detalle (
                id_ajuste_stock_cab INTEGER NOT NULL,
                id_articulo INTEGER NOT NULL,
                aju_stk_cantidad INTEGER NOT NULL,
                id_motivo_ajuste INTEGER NOT NULL,
                id_deposito INTEGER NOT NULL,
                CONSTRAINT id_ajuste_stock_detalle PRIMARY KEY (id_ajuste_stock_cab, id_articulo)
);


CREATE TABLE public.stock (
                id_deposito INTEGER NOT NULL,
                id_articulo INTEGER NOT NULL,
                stk_cantidad_minima INTEGER DEFAULT 0,
                stk_cantidad_maxima INTEGER DEFAULT 0,
                stk_stock_actual INTEGER,
                CONSTRAINT id_stock PRIMARY KEY (id_deposito, id_articulo)
);


CREATE SEQUENCE public.persona_id_persona_seq;

CREATE TABLE public.persona (
                id_persona INTEGER NOT NULL DEFAULT nextval('public.persona_id_persona_seq'),
                per_nombre VARCHAR(50) NOT NULL,
                per_apellido VARCHAR(50) NOT NULL,
                per_ci VARCHAR(15),
                per_telefono VARCHAR(15),
                per_email VARCHAR(50),
                per_direccion VARCHAR(200),
                per_fecha_nac DATE,
                CONSTRAINT id_persona PRIMARY KEY (id_persona)
);


ALTER SEQUENCE public.persona_id_persona_seq OWNED BY public.persona.id_persona;

CREATE SEQUENCE public.titular_id_titular_seq;

CREATE TABLE public.titular (
                id_titular INTEGER NOT NULL DEFAULT nextval('public.titular_id_titular_seq'),
                id_persona INTEGER NOT NULL,
                CONSTRAINT id_titular PRIMARY KEY (id_titular)
);


ALTER SEQUENCE public.titular_id_titular_seq OWNED BY public.titular.id_titular;

CREATE SEQUENCE public.cheque_recibido_id_cheque_reci_seq;

CREATE TABLE public.cheque_recibido (
                id_cheque_reci INTEGER NOT NULL DEFAULT nextval('public.cheque_recibido_id_cheque_reci_seq'),
                cheque_reci_numero INTEGER NOT NULL,
                cheque_reci_serie CHAR NOT NULL,
                cheque_reci_fecha_emision DATE NOT NULL,
                cheque_reci_fecha_venci DATE NOT NULL,
                cheque_reci_fecha_pago DATE NOT NULL,
                cheque_reci_estado VARCHAR(20) NOT NULL,
                cheque_reci_observacion VARCHAR(255) NOT NULL,
                id_tipo_cheque INTEGER NOT NULL,
                id_moneda INTEGER NOT NULL,
                id_titular INTEGER NOT NULL,
                CONSTRAINT id_cheque_reci PRIMARY KEY (id_cheque_reci)
);
COMMENT ON COLUMN public.cheque_recibido.cheque_reci_fecha_venci IS 'no mayor a 180 dias o 6 meses';


ALTER SEQUENCE public.cheque_recibido_id_cheque_reci_seq OWNED BY public.cheque_recibido.id_cheque_reci;

CREATE SEQUENCE public.cliente_id_cliente_seq;

CREATE TABLE public.cliente (
                id_cliente INTEGER NOT NULL DEFAULT nextval('public.cliente_id_cliente_seq'),
                cli_razon_social VARCHAR(100) NOT NULL,
                id_persona INTEGER NOT NULL,
                id_tipo_entidad INTEGER NOT NULL,
                cli_ruc VARCHAR,
                cli_nombre_comercial VARCHAR,
                cli_direccion VARCHAR,
                cli_telefono VARCHAR,
                CONSTRAINT id_cliente PRIMARY KEY (id_cliente)
);


ALTER SEQUENCE public.cliente_id_cliente_seq OWNED BY public.cliente.id_cliente;

CREATE SEQUENCE public.proveedor_id_proveedor_seq;

CREATE TABLE public.proveedor (
                id_proveedor INTEGER NOT NULL DEFAULT nextval('public.proveedor_id_proveedor_seq'),
                prov_razon_social VARCHAR(100) NOT NULL,
                id_persona INTEGER,
                id_tipo_entidad INTEGER NOT NULL,
                prov_ruc VARCHAR,
                prov_nombre_comercial VARCHAR,
                prov_direccion VARCHAR,
                prov_telefono VARCHAR,
                CONSTRAINT id_proveedor PRIMARY KEY (id_proveedor)
);


ALTER SEQUENCE public.proveedor_id_proveedor_seq OWNED BY public.proveedor.id_proveedor;

CREATE SEQUENCE public.provision_cuenta_pagar_id_provi_cta_pagar_cabecera_seq;

CREATE TABLE public.provision_cuenta_pagar (
                id_provi_cta_pagar_cabecera INTEGER NOT NULL DEFAULT nextval('public.provision_cuenta_pagar_id_provi_cta_pagar_cabecera_seq'),
                prov_cta_pag_estado VARCHAR NOT NULL,
                prov_cta_pag_fecha DATE NOT NULL,
                id_proveedor INTEGER NOT NULL,
                CONSTRAINT id_provi_cta_pagar PRIMARY KEY (id_provi_cta_pagar_cabecera)
);
COMMENT ON TABLE public.provision_cuenta_pagar IS 'la provision es por proveedor, primero se selcciona el proveedor y en base a este se traen sus cuentas a pagar (sus facturas con saldo pendiente)';


ALTER SEQUENCE public.provision_cuenta_pagar_id_provi_cta_pagar_cabecera_seq OWNED BY public.provision_cuenta_pagar.id_provi_cta_pagar_cabecera;

CREATE SEQUENCE public.fondo_fijo_id_fondo_fijo_seq;

CREATE TABLE public.fondo_fijo (
                id_fondo_fijo INTEGER NOT NULL DEFAULT nextval('public.fondo_fijo_id_fondo_fijo_seq'),
                fondo_fijo_responsable VARCHAR(50) NOT NULL,
                fondo_fijo_monto_asignado INTEGER NOT NULL,
                fondo_fijo_fecha_asig DATE NOT NULL,
                id_proveedor INTEGER NOT NULL,
                CONSTRAINT id_fondo_fijo PRIMARY KEY (id_fondo_fijo)
);
COMMENT ON TABLE public.fondo_fijo IS 'analizar flujo: FACTURA FONDO FIJO → CUENTA POR PAGAR → RENDICIÓN → PROVISIÓN → ORDEN PAGO o FACTURA FONDO FIJO → RENDICIÓN → PROVISIÓN → ORDEN PAGO (2da opcion es mejor segun IA)';


ALTER SEQUENCE public.fondo_fijo_id_fondo_fijo_seq OWNED BY public.fondo_fijo.id_fondo_fijo;

CREATE TABLE public.fondo_fijo_rendicion (
                id_fondofijo_rendicion INTEGER NOT NULL,
                id_fondo_fijo INTEGER NOT NULL,
                fecha_emision_rendicion DATE,
                ff_rendicion_fecha_reposicion DATE,
                nro_rendicion INTEGER,
                CONSTRAINT id_fondo_fijo_rend PRIMARY KEY (id_fondofijo_rendicion)
);
COMMENT ON TABLE public.fondo_fijo_rendicion IS 'siempre se paga el total de las facturas porque son montos menores analizar flujo: FACTURA FONDO FIJO → CUENTA POR PAGAR → RENDICIÓN → PROVISIÓN → ORDEN PAGO o FACTURA FONDO FIJO → RENDICIÓN → PROVISIÓN → ORDEN PAGO (2da opcion es mejor segun IA)';
COMMENT ON COLUMN public.fondo_fijo_rendicion.nro_rendicion IS 'Numero con el cual se le va a identificar a la rendicion de varias facturas';


CREATE SEQUENCE public.usuario_id_usuario_seq;

CREATE TABLE public.usuario (
                id_usuario INTEGER NOT NULL DEFAULT nextval('public.usuario_id_usuario_seq'),
                id_persona INTEGER NOT NULL,
                usu_user VARCHAR(50) NOT NULL,
                usu_pass VARCHAR(50) NOT NULL,
                usu_estado VARCHAR(20) NOT NULL,
                id_grupo INTEGER NOT NULL,
                CONSTRAINT id_usuario PRIMARY KEY (id_usuario)
);


ALTER SEQUENCE public.usuario_id_usuario_seq OWNED BY public.usuario.id_usuario;

CREATE SEQUENCE public.cheque_id_cheque_seq;

CREATE TABLE public.cheque (
                id_cheque INTEGER NOT NULL DEFAULT nextval('public.cheque_id_cheque_seq'),
                chq_numero INTEGER NOT NULL,
                chq_fecha_emision DATE NOT NULL,
                chq_estado VARCHAR(20) NOT NULL,
                id_chequera INTEGER NOT NULL,
                chq_a_la_orden VARCHAR(80) NOT NULL,
                chq_observacion VARCHAR(255) NOT NULL,
                id_tipo_cheque INTEGER NOT NULL,
                chq_fecha_pago DATE NOT NULL,
                chq_fecha_venci DATE NOT NULL,
                id_usuario INTEGER NOT NULL,
                CONSTRAINT id_cheque PRIMARY KEY (id_cheque)
);


ALTER SEQUENCE public.cheque_id_cheque_seq OWNED BY public.cheque.id_cheque;

CREATE SEQUENCE public.orden_pago_cabecera_id_orden_pago_cabecera_seq;

CREATE TABLE public.orden_pago_cabecera (
                id_orden_pago INTEGER NOT NULL DEFAULT nextval('public.orden_pago_cabecera_id_orden_pago_cabecera_seq'),
                ord_pag_numero INTEGER NOT NULL,
                ord_pag_fecha_emision DATE NOT NULL,
                ord_pag_monto INTEGER NOT NULL,
                ord_pag_estado VARCHAR(100) NOT NULL,
                id_provi_cta_pagar_cabecera INTEGER NOT NULL,
                ord_pag_nro_recibo INTEGER NOT NULL,
                id_moneda INTEGER NOT NULL,
                ord_pag_tipo_cambio DOUBLE PRECISION,
                id_sucursal INTEGER NOT NULL,
                id_cheque INTEGER,
                ord_pag_tipo_pago VARCHAR NOT NULL,
                id_proveedor INTEGER NOT NULL,
                id_cuenta INTEGER NOT NULL,
                CONSTRAINT id_orden_pago PRIMARY KEY (id_orden_pago)
);
COMMENT ON TABLE public.orden_pago_cabecera IS 'No se puede generar una OP sin antes haber hecho una provision de cta a pag';
COMMENT ON COLUMN public.orden_pago_cabecera.ord_pag_nro_recibo IS 'numero de recibo que da el proveedor en caso de que la compra sea a credito';
COMMENT ON COLUMN public.orden_pago_cabecera.ord_pag_tipo_pago IS 'detalle de si el pago es para reposicion de fondo fijo u otro gasto(Aqui se debe seleccionar la opcion de si es reposicion de FF u otros gastos) (aqui debe traer la info de la factura compra fact_comp_tipo_factura)';


ALTER SEQUENCE public.orden_pago_cabecera_id_orden_pago_cabecera_seq OWNED BY public.orden_pago_cabecera.id_orden_pago;

CREATE TABLE public.forma_pago_detalle (
                id_forma_pago_det INTEGER NOT NULL,
                id_forma_pago_cab INTEGER NOT NULL,
                id_orden_pago INTEGER NOT NULL,
                forma_pag_tranferencia INTEGER NOT NULL,
                forma_pag_cheque INTEGER NOT NULL,
                forma_pag_monto INTEGER NOT NULL,
                forma_pag_estado VARCHAR(100),
                forma_pag_referencia VARCHAR,
                id_cuenta INTEGER NOT NULL,
                forma_pag_fecha DATE,
                CONSTRAINT id_forma_pago_det PRIMARY KEY (id_forma_pago_det)
);
COMMENT ON TABLE public.forma_pago_detalle IS 'una orden de pago se puede pagar de varias formas por eso hay una tabla donde se detalla cual orden de pago se pago con transferenia y cheque';
COMMENT ON COLUMN public.forma_pago_detalle.forma_pag_referencia IS 'Nro cheque, transferencia, etc';


CREATE SEQUENCE public.nota_debito_venta_cabecera_id_nota_debi_vent_cab_seq;

CREATE TABLE public.nota_debito_venta_cabecera (
                id_nota_debi_vent_cab INTEGER NOT NULL DEFAULT nextval('public.nota_debito_venta_cabecera_id_nota_debi_vent_cab_seq'),
                nota_debi_venta_numero INTEGER NOT NULL,
                nota_debi_vent_fecha_emision DATE NOT NULL,
                nota_debi_vent_motivo VARCHAR(25) NOT NULL,
                nota_debi_vent_observacion VARCHAR(255) NOT NULL,
                nota_debi_vent_estado VARCHAR(20) NOT NULL,
                id_usuario INTEGER NOT NULL,
                id_timbrado INTEGER NOT NULL,
                CONSTRAINT id_nota_debi_vent_cab PRIMARY KEY (id_nota_debi_vent_cab)
);
COMMENT ON COLUMN public.nota_debito_venta_cabecera.nota_debi_vent_motivo IS 'comisiones, intereses,gasto de flete, etc.';


ALTER SEQUENCE public.nota_debito_venta_cabecera_id_nota_debi_vent_cab_seq OWNED BY public.nota_debito_venta_cabecera.id_nota_debi_vent_cab;

CREATE SEQUENCE public.nota_credito_venta_cabecera_id_nota_ced_venta_cab_seq;

CREATE TABLE public.nota_credito_venta_cabecera (
                id_nota_ced_venta_cab INTEGER NOT NULL DEFAULT nextval('public.nota_credito_venta_cabecera_id_nota_ced_venta_cab_seq'),
                nota_cred_venta_numero INTEGER NOT NULL,
                nota_cred_vent_fecha_emision DATE NOT NULL,
                nota_cred_vent_motivo VARCHAR(25) NOT NULL,
                nota_cred_vent_observacion VARCHAR(255) NOT NULL,
                nota_cred_vent_estado VARCHAR(20) NOT NULL,
                id_usuario INTEGER NOT NULL,
                id_timbrado INTEGER NOT NULL,
                CONSTRAINT id_nota_cred_venta_cab PRIMARY KEY (id_nota_ced_venta_cab)
);
COMMENT ON COLUMN public.nota_credito_venta_cabecera.nota_cred_vent_motivo IS 'anulacion, descuento, devolucion, etc';


ALTER SEQUENCE public.nota_credito_venta_cabecera_id_nota_ced_venta_cab_seq OWNED BY public.nota_credito_venta_cabecera.id_nota_ced_venta_cab;

CREATE SEQUENCE public.pedido_venta_cabecera_id_ped_venta_cab_seq;

CREATE TABLE public.pedido_venta_cabecera (
                id_ped_venta_cab INTEGER NOT NULL DEFAULT nextval('public.pedido_venta_cabecera_id_ped_venta_cab_seq'),
                ped_ven_fecha DATE NOT NULL,
                ped_ven_estado VARCHAR(100) NOT NULL,
                id_cliente INTEGER NOT NULL,
                id_sucursal INTEGER NOT NULL,
                id_usuario INTEGER NOT NULL,
                CONSTRAINT id_ped_venta_cab PRIMARY KEY (id_ped_venta_cab)
);


ALTER SEQUENCE public.pedido_venta_cabecera_id_ped_venta_cab_seq OWNED BY public.pedido_venta_cabecera.id_ped_venta_cab;

CREATE TABLE public.pedido_venta_detalle (
                id_ped_venta_cab INTEGER NOT NULL,
                id_articulo INTEGER NOT NULL,
                ped_ven_cantidad INTEGER NOT NULL,
                CONSTRAINT id_ped_venta_det PRIMARY KEY (id_ped_venta_cab, id_articulo)
);


CREATE SEQUENCE public.apertura_cierre_caja_id_aper_cier_caja_seq;

CREATE TABLE public.apertura_cierre_caja (
                id_aper_cier_caja INTEGER NOT NULL DEFAULT nextval('public.apertura_cierre_caja_id_aper_cier_caja_seq'),
                aper_cier_fecha_apertura TIMESTAMP NOT NULL,
                aper_cier_monto_inicial INTEGER NOT NULL,
                aper_cier_fecha_cierre TIMESTAMP NOT NULL,
                aper_cier_efectivo INTEGER NOT NULL,
                aper_cier_cheque INTEGER NOT NULL,
                aper_cier_tarjeta INTEGER NOT NULL,
                aper_cier_monto_cierre INTEGER NOT NULL,
                aper_cier_estado VARCHAR(20) NOT NULL,
                id_caja INTEGER NOT NULL,
                id_sucursal INTEGER NOT NULL,
                id_usuario INTEGER NOT NULL,
                CONSTRAINT id_aper_cier_caja PRIMARY KEY (id_aper_cier_caja)
);


ALTER SEQUENCE public.apertura_cierre_caja_id_aper_cier_caja_seq OWNED BY public.apertura_cierre_caja.id_aper_cier_caja;

CREATE TABLE public.recaudaciones_depositar_detalle (
                id_rec_depositar INTEGER NOT NULL,
                id_aper_cier_caja INTEGER NOT NULL,
                rec_depositar_efectivo INTEGER,
                rec_depositar_cheque INTEGER,
                CONSTRAINT id_rec_depositar_det PRIMARY KEY (id_rec_depositar, id_aper_cier_caja)
);


CREATE SEQUENCE public.factura_venta_cabecera_id_fact_venta_cab_seq;

CREATE TABLE public.factura_venta_cabecera (
                id_fact_venta_cab INTEGER NOT NULL DEFAULT nextval('public.factura_venta_cabecera_id_fact_venta_cab_seq'),
                fact_venta_fecha_emision DATE NOT NULL,
                fact_venta_numero INTEGER NOT NULL,
                fact_venta_condicion VARCHAR(15) NOT NULL,
                fact_venta_plazo INTEGER NOT NULL,
                fact_venta_observacion VARCHAR(255) NOT NULL,
                fact_venta_estado VARCHAR(100) NOT NULL,
                id_ped_venta_cab INTEGER NOT NULL,
                id_cliente INTEGER NOT NULL,
                id_usuario INTEGER NOT NULL,
                id_sucursal INTEGER NOT NULL,
                id_aper_cier_caja INTEGER NOT NULL,
                id_timbrado INTEGER NOT NULL,
                CONSTRAINT id_fact_venta_cab PRIMARY KEY (id_fact_venta_cab)
);
COMMENT ON COLUMN public.factura_venta_cabecera.fact_venta_condicion IS 'CONTADO o CREDITO';
COMMENT ON COLUMN public.factura_venta_cabecera.id_aper_cier_caja IS 'caja donde se facturó';


ALTER SEQUENCE public.factura_venta_cabecera_id_fact_venta_cab_seq OWNED BY public.factura_venta_cabecera.id_fact_venta_cab;

CREATE SEQUENCE public.cuenta_cobrar_id_cta_cobrar_seq;

CREATE TABLE public.cuenta_cobrar (
                id_cta_cobrar INTEGER NOT NULL DEFAULT nextval('public.cuenta_cobrar_id_cta_cobrar_seq'),
                id_fact_venta_cab INTEGER NOT NULL,
                cta_cob_monto INTEGER NOT NULL,
                cta_cob_fecha DATE NOT NULL,
                cta_cob_saldo INTEGER NOT NULL,
                cta_cob_fecha_venci DATE NOT NULL,
                cta_cob_estado VARCHAR(20) NOT NULL,
                cta_cob_cantidad_cuota INTEGER NOT NULL,
                CONSTRAINT id_cuenta_cobrar PRIMARY KEY (id_cta_cobrar, id_fact_venta_cab)
);


ALTER SEQUENCE public.cuenta_cobrar_id_cta_cobrar_seq OWNED BY public.cuenta_cobrar.id_cta_cobrar;

CREATE TABLE public.nota_debito_venta_detalle (
                id_nota_debi_vent_cab INTEGER NOT NULL,
                id_articulo INTEGER NOT NULL,
                id_fact_venta_cab INTEGER NOT NULL,
                nota_debi_vent_monto INTEGER NOT NULL,
                CONSTRAINT id_nota_debi_vent_det PRIMARY KEY (id_nota_debi_vent_cab, id_articulo, id_fact_venta_cab)
);


CREATE TABLE public.nota_credito_venta_detalle (
                id_nota_ced_venta_cab INTEGER NOT NULL,
                id_articulo INTEGER NOT NULL,
                id_fact_venta_cab INTEGER NOT NULL,
                nota_cred_vent_cantidad INTEGER NOT NULL,
                CONSTRAINT id_nota_cred_vent_det PRIMARY KEY (id_nota_ced_venta_cab, id_articulo, id_fact_venta_cab)
);


CREATE TABLE public.libro_iva_venta (
                id_libro_iva_venta INTEGER NOT NULL,
                id_fact_venta_cab INTEGER NOT NULL,
                id_libro_iva_vent_fecha DATE NOT NULL,
                id_libro_iva_vent_5 INTEGER NOT NULL,
                id_libro_iva_vent_10 INTEGER NOT NULL,
                CONSTRAINT id_libro_iva_venta PRIMARY KEY (id_libro_iva_venta, id_fact_venta_cab)
);


CREATE TABLE public.factura_venta_detalle (
                id_fact_venta_cab INTEGER NOT NULL,
                id_articulo INTEGER NOT NULL,
                id_deposito INTEGER NOT NULL,
                fact_venta_cantidad INTEGER NOT NULL,
                fact_venta_precio_venta INTEGER NOT NULL,
                CONSTRAINT id_fact_venta_det PRIMARY KEY (id_fact_venta_cab, id_articulo, id_deposito)
);


CREATE SEQUENCE public.nota_remision_venta_cabecera_id_nota_remi_venta_seq;

CREATE TABLE public.nota_remision_venta_cabecera (
                id_nota_remi_venta INTEGER NOT NULL DEFAULT nextval('public.nota_remision_venta_cabecera_id_nota_remi_venta_seq'),
                remi_vent_fecha_emision DATE NOT NULL,
                not_remi_vent_descripcion VARCHAR(50) NOT NULL,
                not_remi_vent_estado VARCHAR(20) NOT NULL,
                id_cliente INTEGER NOT NULL,
                id_usuario INTEGER NOT NULL,
                id_fact_venta_cab INTEGER NOT NULL,
                id_timbrado INTEGER NOT NULL,
                CONSTRAINT id_nota_remi_venta PRIMARY KEY (id_nota_remi_venta)
);


ALTER SEQUENCE public.nota_remision_venta_cabecera_id_nota_remi_venta_seq OWNED BY public.nota_remision_venta_cabecera.id_nota_remi_venta;

CREATE TABLE public.nota_remision_venta_detalle (
                id_nota_remi_venta INTEGER NOT NULL,
                id_articulo INTEGER NOT NULL,
                not_remi_vent_cantidad INTEGER NOT NULL,
                CONSTRAINT id_nota_remi_vent_det PRIMARY KEY (id_nota_remi_venta, id_articulo)
);


CREATE SEQUENCE public.cobro_id_cobro_seq;

CREATE TABLE public.cobro (
                id_cobro INTEGER NOT NULL DEFAULT nextval('public.cobro_id_cobro_seq'),
                cob_fecha DATE NOT NULL,
                cob_estado VARCHAR(20) NOT NULL,
                id_aper_cier_caja INTEGER NOT NULL,
                id_usuario INTEGER NOT NULL,
                cob_det_monto INTEGER NOT NULL,
                CONSTRAINT id_cobro PRIMARY KEY (id_cobro)
);


ALTER SEQUENCE public.cobro_id_cobro_seq OWNED BY public.cobro.id_cobro;

CREATE SEQUENCE public.creditos_id_creditos_seq;

CREATE TABLE public.creditos (
                id_creditos INTEGER NOT NULL DEFAULT nextval('public.creditos_id_creditos_seq'),
                creditos_nro_comprobante INTEGER NOT NULL,
                creditos_fecha DATE NOT NULL,
                creditos_detalle VARCHAR(255) NOT NULL,
                id_cuenta INTEGER NOT NULL,
                id_cobro INTEGER NOT NULL,
                credito_monto INTEGER NOT NULL,
                CONSTRAINT id_creditos PRIMARY KEY (id_creditos)
);
COMMENT ON TABLE public.creditos IS 'deposito bancario, comisiones cobradas, capitalizacion de intereses';
COMMENT ON COLUMN public.creditos.creditos_nro_comprobante IS 'comprobante puede ser nro de boleta de deposito';
COMMENT ON COLUMN public.creditos.creditos_detalle IS 'concepto de que se carga el credito, ejemplo ingresos como una venta donde se carga la boleta de deposito';


ALTER SEQUENCE public.creditos_id_creditos_seq OWNED BY public.creditos.id_creditos;

CREATE TABLE public.conciliacion_bancaria_detalle (
                id_conc_bancaria INTEGER NOT NULL,
                conc_bancaria_nro_item INTEGER NOT NULL,
                id_creditos INTEGER,
                id_debitos INTEGER,
                id_orden_pago INTEGER,
                conc_bancaria_descripcion VARCHAR NOT NULL,
                conc_bancaria_monto INTEGER NOT NULL,
                conc_bancaria_tipo VARCHAR NOT NULL,
                conc_bancaria_conciliado BOOLEAN NOT NULL,
                id_forma_pago_det INTEGER NOT NULL,
                CONSTRAINT id_conc_bancaria_det PRIMARY KEY (id_conc_bancaria, conc_bancaria_nro_item)
);
COMMENT ON COLUMN public.conciliacion_bancaria_detalle.conc_bancaria_tipo IS '''Cred''=Crédito, ''Deb''=Débito, etc';


CREATE SEQUENCE public.arqueo_caja_id_arqueo_caja_seq;

CREATE TABLE public.arqueo_caja (
                id_arqueo_caja INTEGER NOT NULL DEFAULT nextval('public.arqueo_caja_id_arqueo_caja_seq'),
                arq_caja_fecha TIMESTAMP NOT NULL,
                id_cobro INTEGER NOT NULL,
                id_aper_cier_caja INTEGER NOT NULL,
                arque_caja_fecha DATE NOT NULL,
                arque_caja_efectivo INTEGER NOT NULL,
                arque_caja_cheque INTEGER NOT NULL,
                arque_caja_tarjeta INTEGER NOT NULL,
                arque_caja_factura_inicial INTEGER NOT NULL,
                arque_caja_factura_final INTEGER NOT NULL,
                arque_caja_observacion VARCHAR(255) NOT NULL,
                id_usuario INTEGER NOT NULL,
                id_caja INTEGER NOT NULL,
                CONSTRAINT id_arqueo_caja PRIMARY KEY (id_arqueo_caja)
);


ALTER SEQUENCE public.arqueo_caja_id_arqueo_caja_seq OWNED BY public.arqueo_caja.id_arqueo_caja;

CREATE SEQUENCE public.cobro_tarjeta_id_cobro_tarjeta_seq;

CREATE TABLE public.cobro_tarjeta (
                id_cobro_tarjeta INTEGER NOT NULL DEFAULT nextval('public.cobro_tarjeta_id_cobro_tarjeta_seq'),
                id_cobro INTEGER NOT NULL,
                id_tarjeta INTEGER NOT NULL,
                tarjeta_nro_boleta_post INTEGER NOT NULL,
                CONSTRAINT id_cobro_tarjeta PRIMARY KEY (id_cobro_tarjeta, id_cobro)
);


ALTER SEQUENCE public.cobro_tarjeta_id_cobro_tarjeta_seq OWNED BY public.cobro_tarjeta.id_cobro_tarjeta;

CREATE SEQUENCE public.cobro_cheque_id_cob_cheque_seq;

CREATE TABLE public.cobro_cheque (
                id_cob_cheque INTEGER NOT NULL DEFAULT nextval('public.cobro_cheque_id_cob_cheque_seq'),
                id_cobro INTEGER NOT NULL,
                id_cheque_reci INTEGER NOT NULL,
                CONSTRAINT id_cob_cheque PRIMARY KEY (id_cob_cheque, id_cobro)
);


ALTER SEQUENCE public.cobro_cheque_id_cob_cheque_seq OWNED BY public.cobro_cheque.id_cob_cheque;

CREATE TABLE public.forma_cobro_detalle (
                id_forma_cobro INTEGER NOT NULL,
                id_cobro INTEGER NOT NULL,
                forma_cob_efectivo INTEGER NOT NULL,
                forma_cob_cheque INTEGER NOT NULL,
                forma_cob_tarjeta INTEGER NOT NULL,
                forma_cob_total INTEGER NOT NULL,
                CONSTRAINT id_forma_cob_det PRIMARY KEY (id_forma_cobro, id_cobro)
);


CREATE TABLE public.cobro_detalle (
                id_cobro INTEGER NOT NULL,
                id_cta_cobrar INTEGER NOT NULL,
                id_fact_venta_cab INTEGER NOT NULL,
                cob_cantidad INTEGER NOT NULL,
                CONSTRAINT id_cobro_detalle PRIMARY KEY (id_cobro, id_cta_cobrar, id_fact_venta_cab)
);


CREATE SEQUENCE public.nota_remision_cabecera_id_nota_rem_cab_seq;

CREATE TABLE public.nota_remision_cabecera (
                id_nota_rem_cab INTEGER NOT NULL DEFAULT nextval('public.nota_remision_cabecera_id_nota_rem_cab_seq'),
                nota_rem_numero INTEGER NOT NULL,
                nota_rem_timbrado INTEGER NOT NULL,
                nota_rem_fecha_venci_timb DATE NOT NULL,
                nota_rem_fecha_emision DATE NOT NULL,
                nota_rem_vehiculo VARCHAR(255),
                nota_rem_conductor VARCHAR(50),
                nota_rem_emisor VARCHAR(50),
                nota_rem_receptor VARCHAR(50),
                nota_rem_estado VARCHAR(100) NOT NULL,
                nota_rem_observacion VARCHAR(255) NOT NULL,
                id_usuario INTEGER NOT NULL,
                id_deposito INTEGER NOT NULL,
                CONSTRAINT id_nota_remision_cab PRIMARY KEY (id_nota_rem_cab)
);
COMMENT ON COLUMN public.nota_remision_cabecera.nota_rem_emisor IS 'persona a cargo de realizar y remitir la remision y los articulos en la remision';
COMMENT ON COLUMN public.nota_remision_cabecera.nota_rem_receptor IS 'persona a cargo de recibir los articulos y la remision';


ALTER SEQUENCE public.nota_remision_cabecera_id_nota_rem_cab_seq OWNED BY public.nota_remision_cabecera.id_nota_rem_cab;

CREATE TABLE public.nota_remision_detalle (
                id_nota_rem_cab INTEGER NOT NULL,
                id_articulo INTEGER NOT NULL,
                nota_rem_cantidad INTEGER NOT NULL,
                CONSTRAINT id_nota_remision PRIMARY KEY (id_nota_rem_cab, id_articulo)
);


CREATE SEQUENCE public.pedido_compra_cabecera_id_pedido_cab_seq;

CREATE TABLE public.pedido_compra_cabecera (
                id_pedido_cab INTEGER NOT NULL DEFAULT nextval('public.pedido_compra_cabecera_id_pedido_cab_seq'),
                id_usuario INTEGER NOT NULL,
                id_sucursal INTEGER NOT NULL,
                ped_comp_fecha DATE NOT NULL,
                ped_comp_estado VARCHAR(100) NOT NULL,
                ped_comp_observacion VARCHAR,
                CONSTRAINT id_pedido_cab PRIMARY KEY (id_pedido_cab)
);
COMMENT ON COLUMN public.pedido_compra_cabecera.ped_comp_estado IS 'posibles estados PENDIENTE - Recién creado COTIZACION - En búsqueda de proveedores PARCIAL - Algunos ítems convertidos a OC COMPLETADO - Todo convertido a OC CANCELADO - Pedido anulado';


ALTER SEQUENCE public.pedido_compra_cabecera_id_pedido_cab_seq OWNED BY public.pedido_compra_cabecera.id_pedido_cab;

CREATE SEQUENCE public.presupuesto_cabecera_id_presupuesto_cab_seq;

CREATE TABLE public.presupuesto_cabecera (
                id_presupuesto_cab INTEGER NOT NULL DEFAULT nextval('public.presupuesto_cabecera_id_presupuesto_cab_seq'),
                id_pedido_cab INTEGER NOT NULL,
                id_proveedor INTEGER NOT NULL,
                presu_cab_fecha DATE NOT NULL,
                presu_cab_estado VARCHAR(100) NOT NULL,
                id_usuario INTEGER NOT NULL,
                presu_fecha_venci DATE,
                presu_cab_observacion VARCHAR,
                presu_cab_condicion_comp VARCHAR,
                CONSTRAINT id_presupuesto_cab PRIMARY KEY (id_presupuesto_cab)
);
COMMENT ON COLUMN public.presupuesto_cabecera.presu_fecha_venci IS 'fecha que vence el presupuesto proveido por el proveedor, seria la validez del presupuesto';


ALTER SEQUENCE public.presupuesto_cabecera_id_presupuesto_cab_seq OWNED BY public.presupuesto_cabecera.id_presupuesto_cab;

CREATE SEQUENCE public.orden_compra_cabecera_id_orden_compra_cab_seq;

CREATE TABLE public.orden_compra_cabecera (
                id_orden_compra_cab INTEGER NOT NULL DEFAULT nextval('public.orden_compra_cabecera_id_orden_compra_cab_seq'),
                id_presupuesto_cab INTEGER,
                id_pedido_cab INTEGER NOT NULL,
                id_proveedor INTEGER NOT NULL,
                id_sucursal INTEGER NOT NULL,
                id_usuario INTEGER NOT NULL,
                ord_comp_fecha DATE NOT NULL,
                ord_comp_estado VARCHAR(100) NOT NULL,
                ord_comp_condicion_comp VARCHAR(15),
                ord_comp_observacion VARCHAR,
                CONSTRAINT id_orden_compra_cab PRIMARY KEY (id_orden_compra_cab)
);
COMMENT ON COLUMN public.orden_compra_cabecera.ord_comp_condicion_comp IS 'Contado o credito';


ALTER SEQUENCE public.orden_compra_cabecera_id_orden_compra_cab_seq OWNED BY public.orden_compra_cabecera.id_orden_compra_cab;

CREATE SEQUENCE public.factura_compra_cabecera_id_fact_comp_cab_seq;

CREATE TABLE public.factura_compra_cabecera (
                id_fact_comp_cab INTEGER NOT NULL DEFAULT nextval('public.factura_compra_cabecera_id_fact_comp_cab_seq'),
                fact_comp_numero VARCHAR NOT NULL,
                fact_comp_timbrado INTEGER,
                fact_comp_fecha_venci_timb DATE NOT NULL,
                fact_comp_fecha_emision DATE NOT NULL,
                fact_comp_fecha_carga DATE NOT NULL,
                fact_comp_condicion VARCHAR(15) NOT NULL,
                fact_comp_plazo INTEGER,
                fact_comp_fecha_venci DATE,
                fact_comp_observacion VARCHAR(255),
                fact_comp_estado VARCHAR(100) NOT NULL,
                fact_comp_tipo_factura VARCHAR NOT NULL,
                id_proveedor INTEGER NOT NULL,
                id_sucursal INTEGER NOT NULL,
                id_usuario INTEGER NOT NULL,
                id_orden_compra_cab INTEGER,
                CONSTRAINT id_fac_comp_cab PRIMARY KEY (id_fact_comp_cab)
);
COMMENT ON COLUMN public.factura_compra_cabecera.fact_comp_timbrado IS 'El timbrado es de 8 digitos, fecha de validez de un timbrado suele ser de 3 meses, 6 meses o 1 año';
COMMENT ON COLUMN public.factura_compra_cabecera.fact_comp_fecha_venci_timb IS 'fecha de validez de un timbrado suele ser de 3 meses, 6 meses o 1 año (por lo general 1 año, 3 o 6 meses es cuadno debe algo)';
COMMENT ON COLUMN public.factura_compra_cabecera.fact_comp_condicion IS 'Contado o credito';
COMMENT ON COLUMN public.factura_compra_cabecera.fact_comp_plazo IS 'plazo total para pagar la factura, en días o meses';
COMMENT ON COLUMN public.factura_compra_cabecera.fact_comp_fecha_venci IS 'Si es una factura a credito, fecha de cuando vence la deuda';
COMMENT ON COLUMN public.factura_compra_cabecera.fact_comp_estado IS 'Pendiente de pago, pagado, anulado';
COMMENT ON COLUMN public.factura_compra_cabecera.fact_comp_tipo_factura IS 'si es del proveedor o si es de los gastos del fonndo fijo (Si es de fondo fijo obviamente no tendra orden de compra, presupuesto ni pedido comp)';
COMMENT ON COLUMN public.factura_compra_cabecera.id_orden_compra_cab IS 'debe permitir nulos por si sea una factura por compra de utiles, servicios, mantenimiento, etc';


ALTER SEQUENCE public.factura_compra_cabecera_id_fact_comp_cab_seq OWNED BY public.factura_compra_cabecera.id_fact_comp_cab;

CREATE SEQUENCE public.factura_compra_detalle_id_fact_comp_det_seq;

CREATE TABLE public.factura_compra_detalle (
                id_fact_comp_det INTEGER NOT NULL DEFAULT nextval('public.factura_compra_detalle_id_fact_comp_det_seq'),
                id_fact_comp_cab INTEGER NOT NULL,
                id_articulo INTEGER,
                id_impuesto INTEGER,
                id_deposito INTEGER,
                fact_comp_cantidad INTEGER,
                fact_comp_precio_compra INTEGER,
                fact_det_descripcion VARCHAR,
                CONSTRAINT id_fact_comp_detalle PRIMARY KEY (id_fact_comp_det)
);
COMMENT ON TABLE public.factura_compra_detalle IS 'Para factura_compra_detalle no se puede usar clave compuesta con id_articulo porque puede ser NULL (para gastos).  Se necesita agregar una columna autoincremental.';
COMMENT ON COLUMN public.factura_compra_detalle.id_impuesto IS 'se agrega el impuesto (IVA 5, 10 o exentas) para cada articulo ya que se debe agregar el impuesto de manera manual si el tipo de factura es por compra de algun mueble o por compra de fondo fijo';
COMMENT ON COLUMN public.factura_compra_detalle.fact_comp_precio_compra IS 'para calculo del iva';
COMMENT ON COLUMN public.factura_compra_detalle.fact_det_descripcion IS 'Para items sin artículo (compra por gasto, mantenimineto,servicio, etc)';


ALTER SEQUENCE public.factura_compra_detalle_id_fact_comp_det_seq OWNED BY public.factura_compra_detalle.id_fact_comp_det;

CREATE SEQUENCE public.cuenta_pagar_id_cta_pagar_seq;

CREATE TABLE public.cuenta_pagar (
                id_cta_pagar INTEGER NOT NULL DEFAULT nextval('public.cuenta_pagar_id_cta_pagar_seq'),
                id_fact_comp_cab INTEGER NOT NULL,
                cta_pag_monto INTEGER NOT NULL,
                cta_pag_estado VARCHAR(100) NOT NULL,
                cta_pag_fecha_venci DATE NOT NULL,
                cta_pag_saldo INTEGER NOT NULL,
                cta_pag_plazo INTEGER,
                CONSTRAINT id_cta_pagar PRIMARY KEY (id_cta_pagar, id_fact_comp_cab)
);
COMMENT ON TABLE public.cuenta_pagar IS 'una factura de fondo fijo está con estado RENDICION PENDIENTE y luego cuando se realiza la rendicion pasa a estado PENDIENTE DE PROVISION o PENDIENTE o algo asi';
COMMENT ON COLUMN public.cuenta_pagar.cta_pag_plazo IS 'plazo total para pagar la factura, en días o meses';


ALTER SEQUENCE public.cuenta_pagar_id_cta_pagar_seq OWNED BY public.cuenta_pagar.id_cta_pagar;

CREATE SEQUENCE public.provision_cuenta_pagar_detalle_id_provi_cta_pagar_detalle_seq;

CREATE TABLE public.provision_cuenta_pagar_detalle (
                id_provi_cta_pagar_detalle INTEGER NOT NULL DEFAULT nextval('public.provision_cuenta_pagar_detalle_id_provi_cta_pagar_detalle_seq'),
                id_provi_cta_pagar_cabecera INTEGER NOT NULL,
                id_cta_pagar INTEGER NOT NULL,
                id_fact_comp_cab INTEGER NOT NULL,
                prov_cta_pag_monto INTEGER NOT NULL,
                CONSTRAINT id_provision_cta_pag PRIMARY KEY (id_provi_cta_pagar_detalle)
);
COMMENT ON TABLE public.provision_cuenta_pagar_detalle IS 'Facturas incluidas en la provision por proveedor';


ALTER SEQUENCE public.provision_cuenta_pagar_detalle_id_provi_cta_pagar_detalle_seq OWNED BY public.provision_cuenta_pagar_detalle.id_provi_cta_pagar_detalle;

CREATE TABLE public.fondo_fijo_rendicion_detalle (
                id_ff_rendicion_detalle INTEGER NOT NULL,
                id_fondofijo_rendicion INTEGER NOT NULL,
                id_cta_pagar INTEGER NOT NULL,
                id_fact_comp_cab INTEGER NOT NULL,
                monto_rendido INTEGER,
                CONSTRAINT fondo_fijo_rendicion_detalle_pk PRIMARY KEY (id_ff_rendicion_detalle)
);


CREATE TABLE public.orden_pago_detalle (
                id_orden_pago INTEGER NOT NULL,
                orden_pag_det_monto INTEGER NOT NULL,
                id_cta_pagar INTEGER NOT NULL,
                id_fact_comp_cab INTEGER NOT NULL,
                CONSTRAINT id_orden_pago_detalle PRIMARY KEY (id_orden_pago)
);


CREATE SEQUENCE public.nota_debito_compra_cabecera_id_nota_debi_comp_cab_seq;

CREATE TABLE public.nota_debito_compra_cabecera (
                id_nota_debi_comp_cab INTEGER NOT NULL DEFAULT nextval('public.nota_debito_compra_cabecera_id_nota_debi_comp_cab_seq'),
                nota_debi_comp_numero VARCHAR NOT NULL,
                nota_debi_comp_timbrado INTEGER NOT NULL,
                nota_debi_comp_fecha_venci_timb DATE NOT NULL,
                nota_debi_comp_fecha_emision DATE NOT NULL,
                nota_debi_comp_fecha_carga DATE NOT NULL,
                nota_debi_comp_estado VARCHAR(100) NOT NULL,
                nota_debi_comp_observacion VARCHAR(255),
                id_usuario INTEGER NOT NULL,
                id_proveedor INTEGER NOT NULL,
                id_fact_comp_cab INTEGER NOT NULL,
                nota_debito_motivo VARCHAR(255),
                CONSTRAINT id_nota_debi_comp_cab PRIMARY KEY (id_nota_debi_comp_cab)
);


ALTER SEQUENCE public.nota_debito_compra_cabecera_id_nota_debi_comp_cab_seq OWNED BY public.nota_debito_compra_cabecera.id_nota_debi_comp_cab;

CREATE SEQUENCE public.nota_debito_compra_detalle_id_nota_debito_det_seq;

CREATE TABLE public.nota_debito_compra_detalle (
                id_nota_debito_det INTEGER NOT NULL DEFAULT nextval('public.nota_debito_compra_detalle_id_nota_debito_det_seq'),
                id_articulo INTEGER,
                nota_debi_comp_cantidad INTEGER NOT NULL,
                nota_debi_monto INTEGER NOT NULL,
                id_impuesto INTEGER,
                nota_debito_descripcion VARCHAR,
                id_nota_debi_comp_cab INTEGER NOT NULL,
                CONSTRAINT id_nota_debi_comp_det PRIMARY KEY (id_nota_debito_det)
);


ALTER SEQUENCE public.nota_debito_compra_detalle_id_nota_debito_det_seq OWNED BY public.nota_debito_compra_detalle.id_nota_debito_det;

CREATE SEQUENCE public.nota_credito_compra_cabecera_id_nota_cred_comp_cab_seq;

CREATE TABLE public.nota_credito_compra_cabecera (
                id_nota_cred_comp_cab INTEGER NOT NULL DEFAULT nextval('public.nota_credito_compra_cabecera_id_nota_cred_comp_cab_seq'),
                nota_cred_comp_numero VARCHAR NOT NULL,
                nota_cred_comp_timbrado INTEGER NOT NULL,
                nota_cred_comp_fecha_venci_timb DATE NOT NULL,
                nota_cred_comp_fecha_emision DATE NOT NULL,
                nota_cred_comp_fecha_carga DATE NOT NULL,
                nota_cred_comp_estado VARCHAR(100) NOT NULL,
                nota_cred_comp_observacion VARCHAR(255),
                id_usuario INTEGER NOT NULL,
                id_proveedor INTEGER NOT NULL,
                id_fact_comp_cab INTEGER NOT NULL,
                nota_cred_motivo VARCHAR(255),
                CONSTRAINT id_nota_cred_comp_cab PRIMARY KEY (id_nota_cred_comp_cab)
);


ALTER SEQUENCE public.nota_credito_compra_cabecera_id_nota_cred_comp_cab_seq OWNED BY public.nota_credito_compra_cabecera.id_nota_cred_comp_cab;

CREATE SEQUENCE public.libro_iva_compra_id_libro_iva_compra_seq;

CREATE TABLE public.libro_iva_compra (
                id_libro_iva_compra INTEGER NOT NULL DEFAULT nextval('public.libro_iva_compra_id_libro_iva_compra_seq'),
                id_fact_comp_cab INTEGER,
                id_nota_cred_comp_cab INTEGER,
                id_nota_debi_comp_cab INTEGER,
                libro_iva_comp_fecha DATE,
                libro_iva_comp_5 INTEGER,
                libro_iva_comp_10 INTEGER,
                libro_iva_comp_gravada_10 INTEGER,
                libro_iva_comp_gravada_5 INTEGER,
                libro_iva_comp_exenta INTEGER,
                libro_iva_comp_total INTEGER,
                libro_iva_comp_estado VARCHAR,
                libro_iva_comp_origen VARCHAR,
                CONSTRAINT id_libro_iva_compra PRIMARY KEY (id_libro_iva_compra)
);
COMMENT ON TABLE public.libro_iva_compra IS 'se guarda el total del IVA5 e IVA10 de las facturas (Se recomienda poner en el informe las gravadas)';
COMMENT ON COLUMN public.libro_iva_compra.libro_iva_comp_gravada_10 IS 'subtotal / 11';
COMMENT ON COLUMN public.libro_iva_compra.libro_iva_comp_gravada_5 IS 'subtotal / 21';
COMMENT ON COLUMN public.libro_iva_compra.libro_iva_comp_exenta IS 'ítems exentos de IVA 0%';
COMMENT ON COLUMN public.libro_iva_compra.libro_iva_comp_total IS 'total general de la factura compra';
COMMENT ON COLUMN public.libro_iva_compra.libro_iva_comp_estado IS 'Activo o Anulado — se pone estado para preservar el registro para trazabilidad fiscal';
COMMENT ON COLUMN public.libro_iva_compra.libro_iva_comp_origen IS 'seria el tipo de comprobante que es la fila (factura, nota de crédito o nota de débito)';


ALTER SEQUENCE public.libro_iva_compra_id_libro_iva_compra_seq OWNED BY public.libro_iva_compra.id_libro_iva_compra;

CREATE SEQUENCE public.nota_credito_compra_detalle_id_nota_credito_det_seq;

CREATE TABLE public.nota_credito_compra_detalle (
                id_nota_credito_det INTEGER NOT NULL DEFAULT nextval('public.nota_credito_compra_detalle_id_nota_credito_det_seq'),
                id_articulo INTEGER,
                id_deposito INTEGER,
                nota_cred_comp_cantidad INTEGER NOT NULL,
                nota_cred_monto INTEGER,
                id_impuesto INTEGER,
                nota_credito_descripcion VARCHAR,
                id_nota_cred_comp_cab INTEGER NOT NULL,
                CONSTRAINT id_nota_cred_comp_det PRIMARY KEY (id_nota_credito_det)
);
COMMENT ON COLUMN public.nota_credito_compra_detalle.id_deposito IS 'campo para hacer el descuento automatico del stock';


ALTER SEQUENCE public.nota_credito_compra_detalle_id_nota_credito_det_seq OWNED BY public.nota_credito_compra_detalle.id_nota_credito_det;

CREATE TABLE public.orden_compra_detalle (
                id_orden_compra_cab INTEGER NOT NULL,
                id_articulo INTEGER NOT NULL,
                ord_comp_det_cantidad INTEGER NOT NULL,
                orden_compr_det_precio_compra INTEGER NOT NULL,
                CONSTRAINT id_orden_compra_det PRIMARY KEY (id_orden_compra_cab, id_articulo)
);


CREATE TABLE public.presupuesto_detalle (
                id_presupuesto_cab INTEGER NOT NULL,
                id_articulo INTEGER NOT NULL,
                presu_det_cantidad INTEGER NOT NULL,
                presu_det_precio_compra INTEGER NOT NULL,
                presu_det_descuento INTEGER,
                CONSTRAINT id_presupuesto_det PRIMARY KEY (id_presupuesto_cab, id_articulo)
);


CREATE TABLE public.pedido_compra_detalle (
                id_pedido_cab INTEGER NOT NULL,
                id_articulo INTEGER NOT NULL,
                ped_comp_det_cantidad INTEGER NOT NULL,
                id_deposito INTEGER NOT NULL,
                CONSTRAINT id_pedido_det PRIMARY KEY (id_pedido_cab, id_articulo)
);


ALTER TABLE public.tarjeta ADD CONSTRAINT tipo_tarjeta_tarjeta_fk
FOREIGN KEY (id_tipo_tarjeta)
REFERENCES public.tipo_tarjeta (id_tipo_tarjeta)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cuenta ADD CONSTRAINT tipo_cuenta_cuenta_fk
FOREIGN KEY (id_tipo_cuenta)
REFERENCES public.tipo_cuenta (id_tipo_cuenta)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.tarjeta ADD CONSTRAINT tipo_cuenta_tarjeta_fk
FOREIGN KEY (id_tipo_cuenta)
REFERENCES public.tipo_cuenta (id_tipo_cuenta)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.entidad_financiera ADD CONSTRAINT tipo_entidad_financiera_entidad_financiera_fk
FOREIGN KEY (id_tipo_enti_finan)
REFERENCES public.tipo_entidad_financiera (id_tipo_enti_finan)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cuenta ADD CONSTRAINT entidad_financiera_cuenta_fk
FOREIGN KEY (id_enti_finan)
REFERENCES public.entidad_financiera (id_enti_finan)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.tarjeta ADD CONSTRAINT entidad_financiera_tarjeta_fk
FOREIGN KEY (id_enti_finan)
REFERENCES public.entidad_financiera (id_enti_finan)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cobro_tarjeta ADD CONSTRAINT tarjeta_cobro_tarjeta_fk
FOREIGN KEY (id_tarjeta)
REFERENCES public.tarjeta (id_tarjeta)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cheque_recibido ADD CONSTRAINT moneda_cheque_recibido_fk
FOREIGN KEY (id_moneda)
REFERENCES public.moneda (id_moneda)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.orden_pago_cabecera ADD CONSTRAINT moneda_orden_pagoorden_pago_cabecera_fk
FOREIGN KEY (id_moneda)
REFERENCES public.moneda (id_moneda)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cuenta ADD CONSTRAINT moneda_cuenta_fk
FOREIGN KEY (id_moneda)
REFERENCES public.moneda (id_moneda)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.chequera ADD CONSTRAINT cuenta_chequera_fk
FOREIGN KEY (id_cuenta)
REFERENCES public.cuenta (id_cuenta)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.creditos ADD CONSTRAINT cuenta_creditos_fk
FOREIGN KEY (id_cuenta)
REFERENCES public.cuenta (id_cuenta)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.debitos ADD CONSTRAINT cuenta_debitos_fk
FOREIGN KEY (id_cuenta)
REFERENCES public.cuenta (id_cuenta)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.conciliacion_bancaria ADD CONSTRAINT cuenta_conciliacion_bancaria_fk
FOREIGN KEY (id_cuenta)
REFERENCES public.cuenta (id_cuenta)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.orden_pago_cabecera ADD CONSTRAINT cuenta_orden_pago_cabecera_fk
FOREIGN KEY (id_cuenta)
REFERENCES public.cuenta (id_cuenta)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.forma_pago_detalle ADD CONSTRAINT cuenta_forma_pago_detalle_fk
FOREIGN KEY (id_cuenta)
REFERENCES public.cuenta (id_cuenta)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.recaudaciones_depositar ADD CONSTRAINT cuenta_recaudaciones_depositar_fk
FOREIGN KEY (id_cuenta_destino)
REFERENCES public.cuenta (id_cuenta)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.conciliacion_bancaria_detalle ADD CONSTRAINT conciliacion_bancaria_conciliacion_bancaria_detalle_fk
FOREIGN KEY (id_conc_bancaria)
REFERENCES public.conciliacion_bancaria (id_conc_bancaria)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.conciliacion_bancaria_detalle ADD CONSTRAINT debitos_conciliacion_bancaria_detalle_fk
FOREIGN KEY (id_debitos)
REFERENCES public.debitos (id_debitos)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cheque ADD CONSTRAINT chequera_cheque_emitido_fk
FOREIGN KEY (id_chequera)
REFERENCES public.chequera (id_chequera)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cheque_recibido ADD CONSTRAINT tipo_cheque_cheque_recibido_fk
FOREIGN KEY (id_tipo_cheque)
REFERENCES public.tipo_cheque (id_tipo_cheque)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cheque ADD CONSTRAINT tipo_cheque_cheque_emitido_fk
FOREIGN KEY (id_tipo_cheque)
REFERENCES public.tipo_cheque (id_tipo_cheque)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.recaudaciones_depositar_detalle ADD CONSTRAINT recaudaciones_depositar_recaudaciones_depositar_detalle_fk
FOREIGN KEY (id_rec_depositar)
REFERENCES public.recaudaciones_depositar (id_rec_depositar)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.timbrado ADD CONSTRAINT tipo_comprobante_timbrado_fk
FOREIGN KEY (id_tipo_comprob)
REFERENCES public.tipo_comprobante (id_tipo_comprob)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_venta_cabecera ADD CONSTRAINT timbrado_factura_venta_cabecera_fk
FOREIGN KEY (id_timbrado)
REFERENCES public.timbrado (id_timbrado)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_credito_venta_cabecera ADD CONSTRAINT timbrado_nota_cedito_venta_cabecera_fk
FOREIGN KEY (id_timbrado)
REFERENCES public.timbrado (id_timbrado)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_debito_venta_cabecera ADD CONSTRAINT timbrado_nota_debito_venta_cabecera_fk
FOREIGN KEY (id_timbrado)
REFERENCES public.timbrado (id_timbrado)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_remision_venta_cabecera ADD CONSTRAINT timbrado_nota_remision_venta_cabecera_fk
FOREIGN KEY (id_timbrado)
REFERENCES public.timbrado (id_timbrado)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.proveedor ADD CONSTRAINT tipo_entidad_proveedor_fk
FOREIGN KEY (id_tipo_entidad)
REFERENCES public.tipo_entidad (id_tipo_entidad)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cliente ADD CONSTRAINT tipo_entidad_cliente_fk
FOREIGN KEY (id_tipo_entidad)
REFERENCES public.tipo_entidad (id_tipo_entidad)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.forma_cobro_detalle ADD CONSTRAINT forma_cobro_forma_cobro_detalle_fk
FOREIGN KEY (id_forma_cobro)
REFERENCES public.forma_cobro_cabecera (id_forma_cobro)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.ajuste_stock_detalle ADD CONSTRAINT motivo_ajuste_ajuste_stock_detalle_fk
FOREIGN KEY (id_motivo_ajuste)
REFERENCES public.motivo_ajuste (id_motivo_ajuste)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.ajuste_stock_detalle ADD CONSTRAINT ajuste_stock_cabecera_ajuste_stock_detalle_fk
FOREIGN KEY (id_ajuste_stock_cab)
REFERENCES public.ajuste_stock_cabecera (id_ajuste_stock_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.forma_pago_detalle ADD CONSTRAINT forma_pago_cabecera_forma_pago_detalle_fk
FOREIGN KEY (id_forma_pago_cab)
REFERENCES public.forma_pago_cabecera (id_forma_pago_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.articulo ADD CONSTRAINT presentacion_articulo_fk
FOREIGN KEY (id_presentacion)
REFERENCES public.presentacion (id_presentacion)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.deposito ADD CONSTRAINT sucursal_deposito_fk
FOREIGN KEY (id_sucursal)
REFERENCES public.sucursal (id_sucursal)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.pedido_compra_cabecera ADD CONSTRAINT sucursal_pedido_compra_cabecera_fk
FOREIGN KEY (id_sucursal)
REFERENCES public.sucursal (id_sucursal)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.orden_compra_cabecera ADD CONSTRAINT sucursal_orden_compra_cabecera_fk
FOREIGN KEY (id_sucursal)
REFERENCES public.sucursal (id_sucursal)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_compra_cabecera ADD CONSTRAINT sucursal_factura_compra_cabecera_fk
FOREIGN KEY (id_sucursal)
REFERENCES public.sucursal (id_sucursal)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.caja ADD CONSTRAINT sucursal_caja_fk
FOREIGN KEY (id_sucursal)
REFERENCES public.sucursal (id_sucursal)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.apertura_cierre_caja ADD CONSTRAINT sucursal_apertura_cierre_caja_fk
FOREIGN KEY (id_sucursal)
REFERENCES public.sucursal (id_sucursal)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.pedido_venta_cabecera ADD CONSTRAINT sucursal_pedido_venta_cabecera_fk
FOREIGN KEY (id_sucursal)
REFERENCES public.sucursal (id_sucursal)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_venta_cabecera ADD CONSTRAINT sucursal_factura_venta_cabecera_fk
FOREIGN KEY (id_sucursal)
REFERENCES public.sucursal (id_sucursal)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.orden_pago_cabecera ADD CONSTRAINT sucursal_orden_pagoorden_pago_cabecera_fk
FOREIGN KEY (id_sucursal)
REFERENCES public.sucursal (id_sucursal)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.apertura_cierre_caja ADD CONSTRAINT caja_apertura_cierre_caja_fk
FOREIGN KEY (id_caja)
REFERENCES public.caja (id_caja)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.arqueo_caja ADD CONSTRAINT caja_arqueo_caja_fk
FOREIGN KEY (id_caja)
REFERENCES public.caja (id_caja)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.articulo ADD CONSTRAINT impuesto_articulo_fk
FOREIGN KEY (id_impuesto)
REFERENCES public.impuesto (id_impuesto)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_compra_detalle ADD CONSTRAINT impuesto_factura_compra_detalle_fk
FOREIGN KEY (id_impuesto)
REFERENCES public.impuesto (id_impuesto)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_credito_compra_detalle ADD CONSTRAINT impuesto_nota_credito_compra_detalle_fk
FOREIGN KEY (id_impuesto)
REFERENCES public.impuesto (id_impuesto)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_debito_compra_detalle ADD CONSTRAINT impuesto_nota_debito_compra_detalle_fk
FOREIGN KEY (id_impuesto)
REFERENCES public.impuesto (id_impuesto)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.usuario ADD CONSTRAINT grupo_usuario_fk
FOREIGN KEY (id_grupo)
REFERENCES public.grupo (id_grupo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.permiso ADD CONSTRAINT grupo_permiso_fk
FOREIGN KEY (id_grupo)
REFERENCES public.grupo (id_grupo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.permiso ADD CONSTRAINT modulo_permiso_fk
FOREIGN KEY (id_modulo)
REFERENCES public.modulo (id_modulo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.articulo ADD CONSTRAINT marca_articulo_fk
FOREIGN KEY (id_marca)
REFERENCES public.marca (id_marca)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.stock ADD CONSTRAINT deposito_stock_fk
FOREIGN KEY (id_deposito)
REFERENCES public.deposito (id_deposito)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.ajuste_stock_detalle ADD CONSTRAINT deposito_ajuste_stock_detalle_fk
FOREIGN KEY (id_deposito)
REFERENCES public.deposito (id_deposito)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_venta_detalle ADD CONSTRAINT deposito_factura_venta_detalle_fk
FOREIGN KEY (id_deposito)
REFERENCES public.deposito (id_deposito)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.pedido_compra_detalle ADD CONSTRAINT deposito_pedido_compra_detalle_fk
FOREIGN KEY (id_deposito)
REFERENCES public.deposito (id_deposito)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_remision_cabecera ADD CONSTRAINT deposito_nota_remision_cabecera_fk
FOREIGN KEY (id_deposito)
REFERENCES public.deposito (id_deposito)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_compra_detalle ADD CONSTRAINT deposito_factura_compra_detalle_fk
FOREIGN KEY (id_deposito)
REFERENCES public.deposito (id_deposito)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_credito_compra_detalle ADD CONSTRAINT deposito_nota_credito_compra_detalle_fk
FOREIGN KEY (id_deposito)
REFERENCES public.deposito (id_deposito)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.articulo ADD CONSTRAINT tipo_articulo_articulo_fk
FOREIGN KEY (id_tipo_articulo)
REFERENCES public.tipo_articulo (id_tipo_articulo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.stock ADD CONSTRAINT articulo_stock_fk
FOREIGN KEY (id_articulo)
REFERENCES public.articulo (id_articulo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.pedido_compra_detalle ADD CONSTRAINT articulo_pedido_detalle_fk
FOREIGN KEY (id_articulo)
REFERENCES public.articulo (id_articulo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.presupuesto_detalle ADD CONSTRAINT articulo_presupuesto_detalle_fk
FOREIGN KEY (id_articulo)
REFERENCES public.articulo (id_articulo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.orden_compra_detalle ADD CONSTRAINT articulo_orden_compra_detalle_fk
FOREIGN KEY (id_articulo)
REFERENCES public.articulo (id_articulo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_remision_detalle ADD CONSTRAINT articulo_nota_remision_detalle_fk
FOREIGN KEY (id_articulo)
REFERENCES public.articulo (id_articulo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.ajuste_stock_detalle ADD CONSTRAINT articulo_ajuste_stock_detalle_fk
FOREIGN KEY (id_articulo)
REFERENCES public.articulo (id_articulo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.pedido_venta_detalle ADD CONSTRAINT articulo_pedido_venta_detalle_fk
FOREIGN KEY (id_articulo)
REFERENCES public.articulo (id_articulo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_remision_venta_detalle ADD CONSTRAINT articulo_nota_remision_venta_detalle_fk
FOREIGN KEY (id_articulo)
REFERENCES public.articulo (id_articulo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_venta_detalle ADD CONSTRAINT articulo_factura_venta_detalle_fk
FOREIGN KEY (id_articulo)
REFERENCES public.articulo (id_articulo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_credito_venta_detalle ADD CONSTRAINT articulo_nota_credito_venta_detalle_fk
FOREIGN KEY (id_articulo)
REFERENCES public.articulo (id_articulo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_debito_venta_detalle ADD CONSTRAINT articulo_nota_debito_venta_detalle_fk
FOREIGN KEY (id_articulo)
REFERENCES public.articulo (id_articulo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_compra_detalle ADD CONSTRAINT articulo_factura_compra_detalle_fk
FOREIGN KEY (id_articulo)
REFERENCES public.articulo (id_articulo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_credito_compra_detalle ADD CONSTRAINT articulo_nota_credito_compra_detalle_fk
FOREIGN KEY (id_articulo)
REFERENCES public.articulo (id_articulo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_debito_compra_detalle ADD CONSTRAINT articulo_nota_debito_compra_detalle_fk
FOREIGN KEY (id_articulo)
REFERENCES public.articulo (id_articulo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.usuario ADD CONSTRAINT persona_usuario_fk
FOREIGN KEY (id_persona)
REFERENCES public.persona (id_persona)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.proveedor ADD CONSTRAINT persona_proveedor_fk
FOREIGN KEY (id_persona)
REFERENCES public.persona (id_persona)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cliente ADD CONSTRAINT persona_cliente_fk
FOREIGN KEY (id_persona)
REFERENCES public.persona (id_persona)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.titular ADD CONSTRAINT persona_titular_fk
FOREIGN KEY (id_persona)
REFERENCES public.persona (id_persona)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cheque_recibido ADD CONSTRAINT titular_cheque_recibido_fk
FOREIGN KEY (id_titular)
REFERENCES public.titular (id_titular)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cobro_cheque ADD CONSTRAINT cheque_recibido_cobro_cheque_fk
FOREIGN KEY (id_cheque_reci)
REFERENCES public.cheque_recibido (id_cheque_reci)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.pedido_venta_cabecera ADD CONSTRAINT cliente_pedido_venta_cabecera_fk
FOREIGN KEY (id_cliente)
REFERENCES public.cliente (id_cliente)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_remision_venta_cabecera ADD CONSTRAINT cliente_nota_remision_venta_cabecera_fk
FOREIGN KEY (id_cliente)
REFERENCES public.cliente (id_cliente)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_venta_cabecera ADD CONSTRAINT cliente_factura_venta_cabecera_fk
FOREIGN KEY (id_cliente)
REFERENCES public.cliente (id_cliente)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.presupuesto_cabecera ADD CONSTRAINT proveedor_presupuesto_cabecera_fk
FOREIGN KEY (id_proveedor)
REFERENCES public.proveedor (id_proveedor)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.orden_compra_cabecera ADD CONSTRAINT proveedor_orden_compra_cabecera_fk
FOREIGN KEY (id_proveedor)
REFERENCES public.proveedor (id_proveedor)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_compra_cabecera ADD CONSTRAINT proveedor_factura_compra_cabecera_fk
FOREIGN KEY (id_proveedor)
REFERENCES public.proveedor (id_proveedor)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.fondo_fijo ADD CONSTRAINT proveedor_fondo_fijo_fk
FOREIGN KEY (id_proveedor)
REFERENCES public.proveedor (id_proveedor)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_credito_compra_cabecera ADD CONSTRAINT proveedor_nota_credito_compra_cabecera_fk
FOREIGN KEY (id_proveedor)
REFERENCES public.proveedor (id_proveedor)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_debito_compra_cabecera ADD CONSTRAINT proveedor_nota_debito_compra_cabecera_fk
FOREIGN KEY (id_proveedor)
REFERENCES public.proveedor (id_proveedor)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.provision_cuenta_pagar ADD CONSTRAINT proveedor_provision_cuenta_pagar_fk
FOREIGN KEY (id_proveedor)
REFERENCES public.proveedor (id_proveedor)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.orden_pago_cabecera ADD CONSTRAINT proveedor_orden_pagoorden_pago_cabecera_fk
FOREIGN KEY (id_proveedor)
REFERENCES public.proveedor (id_proveedor)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.orden_pago_cabecera ADD CONSTRAINT provision_cuenta_pagar_orden_pagoorden_pago_cabecera_fk
FOREIGN KEY (id_provi_cta_pagar_cabecera)
REFERENCES public.provision_cuenta_pagar (id_provi_cta_pagar_cabecera)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.provision_cuenta_pagar_detalle ADD CONSTRAINT provision_cuenta_pagar_provision_cuenta_pagar_detalle_fk
FOREIGN KEY (id_provi_cta_pagar_cabecera)
REFERENCES public.provision_cuenta_pagar (id_provi_cta_pagar_cabecera)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.fondo_fijo_rendicion ADD CONSTRAINT fondo_fijo_fondo_fijo_rendicion_fk
FOREIGN KEY (id_fondo_fijo)
REFERENCES public.fondo_fijo (id_fondo_fijo)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.fondo_fijo_rendicion_detalle ADD CONSTRAINT fondo_fijo_rendicion_fondo_fijo_rendicion_detalle_fk
FOREIGN KEY (id_fondofijo_rendicion)
REFERENCES public.fondo_fijo_rendicion (id_fondofijo_rendicion)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.pedido_compra_cabecera ADD CONSTRAINT usuario_pedido_cabecera_fk
FOREIGN KEY (id_usuario)
REFERENCES public.usuario (id_usuario)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.presupuesto_cabecera ADD CONSTRAINT usuario_presupuesto_cabecera_fk
FOREIGN KEY (id_usuario)
REFERENCES public.usuario (id_usuario)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.orden_compra_cabecera ADD CONSTRAINT usuario_orden_compra_cabecera_fk
FOREIGN KEY (id_usuario)
REFERENCES public.usuario (id_usuario)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_compra_cabecera ADD CONSTRAINT usuario_factura_compra_cabecera_fk
FOREIGN KEY (id_usuario)
REFERENCES public.usuario (id_usuario)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_remision_cabecera ADD CONSTRAINT usuario_nota_remision_cabecera_fk
FOREIGN KEY (id_usuario)
REFERENCES public.usuario (id_usuario)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_credito_compra_cabecera ADD CONSTRAINT usuario_nota_credito_compra_cabecera_fk
FOREIGN KEY (id_usuario)
REFERENCES public.usuario (id_usuario)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_debito_compra_cabecera ADD CONSTRAINT usuario_nota_debito_compra_cabecera_fk
FOREIGN KEY (id_usuario)
REFERENCES public.usuario (id_usuario)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.apertura_cierre_caja ADD CONSTRAINT usuario_apertura_cierre_caja_fk
FOREIGN KEY (id_usuario)
REFERENCES public.usuario (id_usuario)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cobro ADD CONSTRAINT usuario_cobro_fk
FOREIGN KEY (id_usuario)
REFERENCES public.usuario (id_usuario)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.pedido_venta_cabecera ADD CONSTRAINT usuario_pedido_venta_cabecera_fk
FOREIGN KEY (id_usuario)
REFERENCES public.usuario (id_usuario)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_remision_venta_cabecera ADD CONSTRAINT usuario_nota_remision_venta_cabecera_fk
FOREIGN KEY (id_usuario)
REFERENCES public.usuario (id_usuario)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_venta_cabecera ADD CONSTRAINT usuario_factura_venta_cabecera_fk
FOREIGN KEY (id_usuario)
REFERENCES public.usuario (id_usuario)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_credito_venta_cabecera ADD CONSTRAINT usuario_nota_cedito_venta_cabecera_fk
FOREIGN KEY (id_usuario)
REFERENCES public.usuario (id_usuario)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_debito_venta_cabecera ADD CONSTRAINT usuario_nota_debito_venta_cabecera_fk
FOREIGN KEY (id_usuario)
REFERENCES public.usuario (id_usuario)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.arqueo_caja ADD CONSTRAINT usuario_arqueo_caja_fk
FOREIGN KEY (id_usuario)
REFERENCES public.usuario (id_usuario)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cheque ADD CONSTRAINT usuario_cheque_emitido_fk
FOREIGN KEY (id_usuario)
REFERENCES public.usuario (id_usuario)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.orden_pago_cabecera ADD CONSTRAINT cheque_emitido_orden_pagoorden_pago_cabecera_fk
FOREIGN KEY (id_cheque)
REFERENCES public.cheque (id_cheque)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.orden_pago_detalle ADD CONSTRAINT orden_pago_orden_pago_detalle_fk
FOREIGN KEY (id_orden_pago)
REFERENCES public.orden_pago_cabecera (id_orden_pago)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.conciliacion_bancaria_detalle ADD CONSTRAINT orden_pagoorden_pago_cabecera_conciliacion_bancaria_detalle_fk
FOREIGN KEY (id_orden_pago)
REFERENCES public.orden_pago_cabecera (id_orden_pago)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.forma_pago_detalle ADD CONSTRAINT orden_pago_cabecera_forma_pago_detalle_fk
FOREIGN KEY (id_orden_pago)
REFERENCES public.orden_pago_cabecera (id_orden_pago)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.conciliacion_bancaria_detalle ADD CONSTRAINT forma_pago_detalle_conciliacion_bancaria_detalle_fk
FOREIGN KEY (id_forma_pago_det)
REFERENCES public.forma_pago_detalle (id_forma_pago_det)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_debito_venta_detalle ADD CONSTRAINT nota_debito_venta_cabecera_nota_debito_venta_detalle_fk
FOREIGN KEY (id_nota_debi_vent_cab)
REFERENCES public.nota_debito_venta_cabecera (id_nota_debi_vent_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_credito_venta_detalle ADD CONSTRAINT nota_credito_venta_cabecera_nota_credito_venta_detalle_fk
FOREIGN KEY (id_nota_ced_venta_cab)
REFERENCES public.nota_credito_venta_cabecera (id_nota_ced_venta_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.pedido_venta_detalle ADD CONSTRAINT pedido_venta_cabecera_pedido_venta_detalle_fk
FOREIGN KEY (id_ped_venta_cab)
REFERENCES public.pedido_venta_cabecera (id_ped_venta_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_venta_cabecera ADD CONSTRAINT pedido_venta_cabecera_factura_venta_cabecera_fk
FOREIGN KEY (id_ped_venta_cab)
REFERENCES public.pedido_venta_cabecera (id_ped_venta_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cobro ADD CONSTRAINT apertura_cierre_caja_cobro_fk
FOREIGN KEY (id_aper_cier_caja)
REFERENCES public.apertura_cierre_caja (id_aper_cier_caja)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_venta_cabecera ADD CONSTRAINT apertura_cierre_caja_factura_venta_cabecera_fk
FOREIGN KEY (id_aper_cier_caja)
REFERENCES public.apertura_cierre_caja (id_aper_cier_caja)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.recaudaciones_depositar_detalle ADD CONSTRAINT apertura_cierre_caja_recaudaciones_depositar_detalle_fk
FOREIGN KEY (id_aper_cier_caja)
REFERENCES public.apertura_cierre_caja (id_aper_cier_caja)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.arqueo_caja ADD CONSTRAINT apertura_cierre_caja_arqueo_caja_fk
FOREIGN KEY (id_aper_cier_caja)
REFERENCES public.apertura_cierre_caja (id_aper_cier_caja)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_remision_venta_cabecera ADD CONSTRAINT factura_venta_cabecera_nota_remision_venta_cabecera_fk
FOREIGN KEY (id_fact_venta_cab)
REFERENCES public.factura_venta_cabecera (id_fact_venta_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_venta_detalle ADD CONSTRAINT factura_venta_cabecera_factura_venta_detalle_fk
FOREIGN KEY (id_fact_venta_cab)
REFERENCES public.factura_venta_cabecera (id_fact_venta_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.libro_iva_venta ADD CONSTRAINT factura_venta_cabecera_libro_iva_venta_fk
FOREIGN KEY (id_fact_venta_cab)
REFERENCES public.factura_venta_cabecera (id_fact_venta_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_credito_venta_detalle ADD CONSTRAINT factura_venta_cabecera_nota_credito_venta_detalle_fk
FOREIGN KEY (id_fact_venta_cab)
REFERENCES public.factura_venta_cabecera (id_fact_venta_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_debito_venta_detalle ADD CONSTRAINT factura_venta_cabecera_nota_debito_venta_detalle_fk
FOREIGN KEY (id_fact_venta_cab)
REFERENCES public.factura_venta_cabecera (id_fact_venta_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cuenta_cobrar ADD CONSTRAINT factura_venta_cabecera_cuenta_cobrar_fk
FOREIGN KEY (id_fact_venta_cab)
REFERENCES public.factura_venta_cabecera (id_fact_venta_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cobro_detalle ADD CONSTRAINT cuenta_cobrar_cobro_detalle_fk
FOREIGN KEY (id_cta_cobrar, id_fact_venta_cab)
REFERENCES public.cuenta_cobrar (id_cta_cobrar, id_fact_venta_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_remision_venta_detalle ADD CONSTRAINT nota_remision_venta_cabecera_nota_remision_venta_detalle_fk
FOREIGN KEY (id_nota_remi_venta)
REFERENCES public.nota_remision_venta_cabecera (id_nota_remi_venta)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cobro_detalle ADD CONSTRAINT cobro_cobro_detalle_fk
FOREIGN KEY (id_cobro)
REFERENCES public.cobro (id_cobro)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.forma_cobro_detalle ADD CONSTRAINT cobro_forma_cobro_detalle_fk
FOREIGN KEY (id_cobro)
REFERENCES public.cobro (id_cobro)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cobro_cheque ADD CONSTRAINT cobro_cobro_cheque_fk
FOREIGN KEY (id_cobro)
REFERENCES public.cobro (id_cobro)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cobro_tarjeta ADD CONSTRAINT cobro_cobro_tarjeta_fk
FOREIGN KEY (id_cobro)
REFERENCES public.cobro (id_cobro)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.arqueo_caja ADD CONSTRAINT cobro_arqueo_caja_fk
FOREIGN KEY (id_cobro)
REFERENCES public.cobro (id_cobro)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.creditos ADD CONSTRAINT cobro_creditos_fk
FOREIGN KEY (id_cobro)
REFERENCES public.cobro (id_cobro)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.conciliacion_bancaria_detalle ADD CONSTRAINT creditos_conciliacion_bancaria_detalle_fk
FOREIGN KEY (id_creditos)
REFERENCES public.creditos (id_creditos)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_remision_detalle ADD CONSTRAINT nota_remision_cabecera_nota_remision_detalle_fk
FOREIGN KEY (id_nota_rem_cab)
REFERENCES public.nota_remision_cabecera (id_nota_rem_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.pedido_compra_detalle ADD CONSTRAINT pedido_cabecera_pedido_detalle_fk
FOREIGN KEY (id_pedido_cab)
REFERENCES public.pedido_compra_cabecera (id_pedido_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.presupuesto_cabecera ADD CONSTRAINT pedido_cabecera_presupuesto_cabecera_fk
FOREIGN KEY (id_pedido_cab)
REFERENCES public.pedido_compra_cabecera (id_pedido_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.orden_compra_cabecera ADD CONSTRAINT pedido_compra_cabecera_orden_compra_cabecera_fk
FOREIGN KEY (id_pedido_cab)
REFERENCES public.pedido_compra_cabecera (id_pedido_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.presupuesto_detalle ADD CONSTRAINT presupuesto_cabecera_presupuesto_detalle_fk
FOREIGN KEY (id_presupuesto_cab)
REFERENCES public.presupuesto_cabecera (id_presupuesto_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.orden_compra_cabecera ADD CONSTRAINT presupuesto_cabecera_orden_compra_cabecera_fk
FOREIGN KEY (id_presupuesto_cab)
REFERENCES public.presupuesto_cabecera (id_presupuesto_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.orden_compra_detalle ADD CONSTRAINT orden_compra_cabecera_orden_compra_detalle_fk
FOREIGN KEY (id_orden_compra_cab)
REFERENCES public.orden_compra_cabecera (id_orden_compra_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_compra_cabecera ADD CONSTRAINT orden_compra_cabecera_factura_compra_cabecera_fk
FOREIGN KEY (id_orden_compra_cab)
REFERENCES public.orden_compra_cabecera (id_orden_compra_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_credito_compra_cabecera ADD CONSTRAINT factura_compra_cabecera_nota_credito_compra_cabecera_fk
FOREIGN KEY (id_fact_comp_cab)
REFERENCES public.factura_compra_cabecera (id_fact_comp_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_debito_compra_cabecera ADD CONSTRAINT factura_compra_cabecera_nota_debito_compra_cabecera_fk
FOREIGN KEY (id_fact_comp_cab)
REFERENCES public.factura_compra_cabecera (id_fact_comp_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.cuenta_pagar ADD CONSTRAINT factura_compra_cabecera_cuenta_pagar_fk
FOREIGN KEY (id_fact_comp_cab)
REFERENCES public.factura_compra_cabecera (id_fact_comp_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.factura_compra_detalle ADD CONSTRAINT factura_compra_cabecera_factura_compra_detalle_fk
FOREIGN KEY (id_fact_comp_cab)
REFERENCES public.factura_compra_cabecera (id_fact_comp_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.libro_iva_compra ADD CONSTRAINT factura_compra_cabecera_libro_iva_compra_fk
FOREIGN KEY (id_fact_comp_cab)
REFERENCES public.factura_compra_cabecera (id_fact_comp_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.orden_pago_detalle ADD CONSTRAINT cuenta_pagar_orden_pago_detalle_fk
FOREIGN KEY (id_cta_pagar, id_fact_comp_cab)
REFERENCES public.cuenta_pagar (id_cta_pagar, id_fact_comp_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.fondo_fijo_rendicion_detalle ADD CONSTRAINT cuenta_pagar_fondo_fijo_rendicion_detalle_fk
FOREIGN KEY (id_cta_pagar, id_fact_comp_cab)
REFERENCES public.cuenta_pagar (id_cta_pagar, id_fact_comp_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.provision_cuenta_pagar_detalle ADD CONSTRAINT cuenta_pagar_provision_cuenta_pagar_detalle_fk
FOREIGN KEY (id_cta_pagar, id_fact_comp_cab)
REFERENCES public.cuenta_pagar (id_cta_pagar, id_fact_comp_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_debito_compra_detalle ADD CONSTRAINT nota_debito_compra_cabecera_nota_debito_compra_detalle_fk
FOREIGN KEY (id_nota_debi_comp_cab)
REFERENCES public.nota_debito_compra_cabecera (id_nota_debi_comp_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.libro_iva_compra ADD CONSTRAINT nota_debito_compra_cabecera_libro_iva_compra_fk
FOREIGN KEY (id_nota_debi_comp_cab)
REFERENCES public.nota_debito_compra_cabecera (id_nota_debi_comp_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.nota_credito_compra_detalle ADD CONSTRAINT nota_credito_compra_cabecera_nota_credito_compra_detalle_fk
FOREIGN KEY (id_nota_cred_comp_cab)
REFERENCES public.nota_credito_compra_cabecera (id_nota_cred_comp_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.libro_iva_compra ADD CONSTRAINT nota_credito_compra_cabecera_libro_iva_compra_fk
FOREIGN KEY (id_nota_cred_comp_cab)
REFERENCES public.nota_credito_compra_cabecera (id_nota_cred_comp_cab)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;