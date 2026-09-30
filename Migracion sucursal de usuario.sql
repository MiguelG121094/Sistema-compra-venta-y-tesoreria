-- Sucursal del usuario: la sucursal deja de elegirse en cada pantalla y sale del usuario logueado.
--
-- Motivo: hoy el operador elige la sucursal en cada documento (pedido, orden de compra, factura de
-- compra y orden de pago), que siempre es la misma. Ademas Ventas la va a necesitar para atar al
-- cajero con la caja de su sucursal (caja.id_sucursal y apertura_cierre_caja).
--
-- La columna es NULLABLE a proposito: los usuarios que ya existen se completan a mano. Mientras un
-- usuario no la tenga, las pantallas avisan ("El usuario no tiene una sucursal asignada") en vez de
-- dejar cargar un documento sin sucursal.
--
-- Acordarse de cambiarlo tambien en el Power Architect, para que el modelo no quede atras.

ALTER TABLE public.usuario ADD COLUMN id_sucursal INTEGER;

ALTER TABLE public.usuario
    ADD CONSTRAINT usuario_sucursal_fk FOREIGN KEY (id_sucursal)
    REFERENCES public.sucursal (id_sucursal)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;

-- Asignacion a los usuarios que ya existen (ajustar los ids segun corresponda).
-- Con una sola sucursal cargada alcanza con la primera linea.
UPDATE public.usuario SET id_sucursal = (SELECT MIN(id_sucursal) FROM public.sucursal)
 WHERE id_sucursal IS NULL;

-- UPDATE public.usuario SET id_sucursal = 2 WHERE usu_user = 'usuario_de_la_otra_sucursal';

-- Control: no deberia quedar ninguno sin sucursal.
-- SELECT id_usuario, usu_user, id_sucursal FROM public.usuario ORDER BY id_usuario;


-- Establecimiento de la sucursal: el primer tramo del numero de comprobante paraguayo
-- (001-002-0000123). El segundo tramo es el punto de expedicion, que identifica a la caja o
-- terminal y ya existe en caja.caja_nro_expedicion; el tercero es el correlativo del comprobante.
--
-- Va como VARCHAR porque son tres digitos con ceros a la izquierda: en INTEGER el '001' se guarda
-- como 1 y hay que rearmarlo al imprimir. Mismo criterio que fact_comp_numero.

ALTER TABLE public.sucursal ADD COLUMN suc_establecimiento VARCHAR(3);

-- Con una sola sucursal, su establecimiento suele ser el 001.
UPDATE public.sucursal SET suc_establecimiento = '001'
 WHERE suc_establecimiento IS NULL AND id_sucursal = (SELECT MIN(id_sucursal) FROM public.sucursal);
