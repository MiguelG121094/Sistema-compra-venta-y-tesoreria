# Conciliación bancaria — análisis y plan

Documento propio del sub-módulo, como `NOTA_CREDITO_DEBITO_PLAN.md`. Cierra el requerimiento **3.10**
y es el objetivo final del módulo de Tesorería (§F de `MODULO_TESORERIA_PLAN.md`).

**Estado al 2026-09-04:** analizado, sin implementar. Las tablas y los POJOs existen y están
alineados; falta todo el resto (DAOs, Service, Servlet, JSP).

---

## 1. Qué resuelve

Conciliar es **cruzar lo que el sistema dice que pasó en una cuenta bancaria contra lo que dice el
extracto del banco**, en un período, y explicar la diferencia.

El resultado de una conciliación es una respuesta a: *"mi sistema dice que en la cuenta de Itaú tengo
X y el banco dice Y — ¿por qué?"*. La diferencia casi nunca es un error: son **partidas
conciliatorias**, movimientos registrados de un lado y todavía no del otro. El caso típico es el
cheque emitido y entregado que el proveedor no fue a cobrar: el sistema ya lo descontó, el banco
todavía no.

Es el último sub-módulo porque necesita que existan las tres fuentes de movimiento, y las tres ya
están: **órdenes de pago** (§C), **débitos** y **créditos** (§D).

---

## 2. Estructura actual

### 2.1 Tablas

```
conciliacion_bancaria                       -- cabecera: una cuenta, un período
    id_conc_bancaria            serial
    id_cuenta                   NOT NULL    -- se concilia cuenta por cuenta
    conc_bancaria_fecha_desde   NOT NULL    -- período: desde
    conc_bancaria_fecha_hasta   NOT NULL    -- período: hasta
    conc_bancaria_fecha         NOT NULL    -- cuándo se hizo la conciliación
    conc_bancaria_saldo_inicial NOT NULL
    conc_bancaria_saldo_final   NOT NULL    -- calculado por el sistema
    conc_banc_saldo_banco       NOT NULL    -- el del extracto, lo carga el usuario

conciliacion_bancaria_detalle               -- los ítems del período
    id_conc_bancaria            NOT NULL  ┐ PK compuesta
    conc_bancaria_nro_item      NOT NULL  ┘ (no hay serial: el nro lo asigna la aplicación)
    id_creditos                 NULL      ┐
    id_debitos                  NULL      │ el ítem apunta a UNA de estas
    id_orden_pago               NULL      │
    id_forma_pago_det           NULL      ┘
    conc_bancaria_descripcion   NOT NULL
    conc_bancaria_monto         NOT NULL
    conc_bancaria_tipo          NOT NULL   -- 'Cred' / 'Deb'
    conc_bancaria_conciliado    BOOLEAN NOT NULL
```

### 2.2 POJOs

`ConciliacionBancaria` y `ConciliacionBancariaDetalle` **están completos y alineados** con las tablas,
incluidos los cuatro enlaces del detalle. No hay que tocarlos.

### 2.3 Lo que falta

`ConciliacionBancariaDAO` (+ detalle), `ConciliacionBancariaService`, `ConciliacionBancariaServlet`,
`conciliacionBancaria.jsp`, el registro en `AuthorizationFilter` y el link del menú.

---

## 3. Dos cosas que hay que entender antes de escribir código

### 3.1 El comentario de la tabla describe un diseño que no es el que se implementó

`conciliacion_bancaria` dice: *"cuando se guarda una orden de pago viene y guarda tambien en la
conciliacion la orden de pago que se hizo en cheque o transferencia"*. **Eso no es lo que hace el
sistema, y no podría hacerlo:** el detalle de conciliación cuelga de una cabecera que es *por cuenta y
por período*, y cuando se genera la OP esa cabecera no existe todavía. Por eso `OrdenPagoService`
nunca escribió en conciliación, que fue una decisión consciente.

El diseño correcto es al revés: **la conciliación es una foto que se arma cuando se concilia.** Se
elige cuenta y período, y el sistema sale a buscar los movimientos. El detalle no se alimenta desde
los otros módulos, se llena en el momento.

### 3.2 La unidad que se concilia NO es la orden de pago: es la forma de pago

Una OP puede pagarse con una transferencia de la cuenta de Itaú y dos cheques de la cuenta de Ueno. En
el extracto de Itaú aparece **una** línea, no la OP entera. Por eso el detalle tiene los dos enlaces:

- `id_forma_pago_det` → **el movimiento real**: `forma_pago_detalle` tiene `id_cuenta`,
  `forma_pag_monto` y `forma_pag_fecha`, que es exactamente lo que hay que cruzar.
- `id_orden_pago` → el documento del que salió, para poder describirlo en la grilla.

Conciliar por OP daría montos que no existen en ningún extracto y rompería la cuenta apenas alguien
pague una OP con dos cuentas distintas.

---

## 4. De dónde salen los movimientos del período

Para una cuenta y un rango de fechas, el sistema tiene que traer tres cosas. Todo lo necesario ya se
graba: `OrdenPagoServlet` completa `forma_pag_fecha` con la fecha de emisión de la OP y deja
`forma_pag_estado` en `'Pendiente'`, justamente esperando a este módulo.

| Origen | Filtro | Tipo | Enlace del ítem |
|---|---|---|---|
| `forma_pago_detalle` | `id_cuenta` = la cuenta, `forma_pag_fecha` en el rango, OP no anulada | `Deb` | `id_forma_pago_det` + `id_orden_pago` |
| `debitos` | `id_cuenta` = la cuenta, `debitos_fecha` en el rango, estado `'Vigente'` | `Deb` | `id_debitos` |
| `creditos` | `id_cuenta` = la cuenta, `creditos_fecha` en el rango, estado `'Vigente'` | `Cred` | `id_creditos` |

**No hace falta convertir monedas.** La conciliación es por cuenta, y todos los movimientos de una
cuenta están en su moneda. El tipo de cambio (`forma_pag_tipo_cambio`, `debitos_tipo_cambio`) sirve
para llevar a guaraníes, que es otro problema.

---

## 5. Los saldos

```
saldo_final = saldo_inicial + Σ(créditos conciliados) − Σ(débitos conciliados)
```

- **`saldo_inicial`**: el saldo con el que arranca el período. Lo natural es tomarlo del
  `saldo_final` de la conciliación anterior de esa misma cuenta, y pedirlo a mano sólo la primera vez.
- **`saldo_final`**: lo calcula el sistema. Ojo: sólo debería sumar **lo conciliado**, si no siempre
  daría igual al saldo del sistema y la conciliación no serviría de nada.
- **`saldo_banco`**: lo carga el usuario leyendo el extracto.

La pantalla tiene que mostrar la **diferencia** entre los dos últimos bien a la vista. Si es cero,
la conciliación cierra. Si no, lo que queda sin tildar es la explicación.

> `cuenta` no tiene columna de saldo, así que no hay un "saldo del sistema" que actualizar. El saldo
> vive únicamente en las conciliaciones, encadenado de una a la siguiente.

---

## 6. Cheques en tránsito

Es la partida conciliatoria clásica y el motivo principal por el que este módulo existe.

Un cheque emitido en marzo y entregado al proveedor puede cobrarse en mayo. En la conciliación de
marzo **aparece pero no se tilda**: el sistema lo descontó, el banco todavía no. En la de mayo, cuando
figure en el extracto, se tilda.

Hoy `chq_estado` contempla `'Cobrado'` —lo dice el comentario de la columna y el javadoc de `Cheque`—
pero **nada lo setea nunca**. La conciliación es el lugar natural: al tildar el ítem de un cheque,
el cheque pasa a `'Cobrado'`. Ver decisión D3.

Lo mismo con `forma_pag_estado`, que nace en `'Pendiente'` esperando a este módulo: al tildar el ítem
debería pasar a `'Conciliado'`.

---

## 7. Flujo propuesto de la pantalla

Patrón documento, como la Orden de Pago: **Nuevo / Buscar / Generar / Cancelar**, con Session+Token
porque hay una grilla que se sostiene entre pedidos.

1. **Nuevo** habilita la cabecera. El usuario elige **cuenta** y **período** (desde / hasta).
2. Al confirmar el período, el sistema **trae los movimientos** de la tabla de §4 y arma la grilla,
   todos destildados. El `saldo_inicial` se propone desde la conciliación anterior de esa cuenta.
3. El usuario carga el **saldo del banco** del extracto y va **tildando** los ítems que encuentra en él.
4. La pantalla recalcula en vivo el **saldo final** y la **diferencia contra el banco**.
5. **Generar** guarda la cabecera y el detalle —incluidos los ítems no tildados, que son la
   explicación de la diferencia— y actualiza los estados de lo conciliado.

---

## 8. Decisiones abiertas

| # | Decisión | Por qué importa |
|---|---|---|
| **D1** | ¿La cabecera necesita un estado para poder anular o reabrir una conciliación? | No hay columna. Sin ella, una conciliación mal hecha no se deshace, y los estados que haya tocado (cheques `'Cobrado'`, formas `'Conciliado'`) quedan mal para siempre. Es el mismo caso de `debitos_estado` y `ff_rendicion_estado`. |
| **D2** | ¿El saldo inicial se encadena desde la conciliación anterior o lo carga el usuario? | Encadenarlo evita errores de tipeo y hace que la serie cierre sola, pero obliga a conciliar en orden y sin huecos. |
| **D3** | Al tildar un ítem de cheque, ¿el cheque pasa a `'Cobrado'`? | Es el único lugar del sistema donde ese estado tendría sentido. Si no, `'Cobrado'` queda muerto para siempre. |
| **D4** | ¿Se puede tildar un ítem que el banco muestra pero el sistema no tiene? | Sería un movimiento no registrado (una comisión que nadie cargó). Lo correcto es cargarlo primero como débito y volver; pero se puede permitir agregarlo desde acá. |
| **D5** | ¿El extracto se importa o se tilda a mano? | Importar (CSV) es mucho más cómodo con volumen, pero cada banco tiene su formato. Tildar a mano no necesita nada y sirve para empezar. |

---

## 9. Riesgos y particularidades

- **PK compuesta sin serial** en el detalle: `conc_bancaria_nro_item` lo asigna la aplicación (1..N
  dentro de la conciliación). No es un problema, pero hay que recordarlo al insertar.
- **Anular una OP o un débito después de conciliarlo.** Si el movimiento ya está en una conciliación
  cerrada, anularlo deja la conciliación mintiendo. Hoy nada lo impide. Depende de D1.
- **Períodos solapados o con huecos.** Nada impide conciliar dos veces el mismo mes, ni saltarse uno.
  Si el saldo inicial se encadena (D2), conviene validar que el `desde` sea el día siguiente al
  `hasta` de la conciliación anterior de esa cuenta.
- **Montos `INTEGER`**: aplica lo mismo que al resto del módulo (§8 del plan de tesorería).
- **`conc_bancaria_tipo`** es texto libre (`'Cred'` / `'Deb'`), sin constraint. Definir las constantes
  en un solo lugar del código.

---

## 10. Componentes a construir

1. `ConciliacionBancariaDAO` — cabecera y detalle en el mismo DAO, como `ProvisionCuentaPagarDAO`.
   Incluye la consulta de movimientos del período (§4) y el `saldo_final` de la conciliación anterior.
2. `ConciliacionBancariaService` — transaccional: guarda cabecera + detalle y actualiza los estados de
   lo conciliado (`forma_pag_estado`, `chq_estado`) en una sola unidad.
3. `ConciliacionBancariaServlet` — Session+Token, calcado de `OrdenPagoServlet`.
4. `conciliacionBancaria.jsp` — cabecera + grilla con checkbox por ítem y el recuadro de saldos.
5. Registro en `AuthorizationFilter` (módulo `tesoreria`) y link en `menuLateral.jsp`.
