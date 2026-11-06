
package com.doncerdito.app

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

data class Producto(
    val id: Long,
    val nombre: String,
    val costo: Double,
    val precio: Double,
    val stock: Double
)

data class Venta(
    val id: Long,
    val productoId: Long,
    val producto: String,
    val libras: Double,
    val total: Double,
    val credito: Boolean,
    val cliente: String
)

data class Gasto(
    val id: Long,
    val concepto: String,
    val valor: Double
)

data class Credito(
    val id: Long,
    val cliente: String,
    val saldo: Double
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DonCerdito(this)
            }
        }
    }
}

@Composable
fun DonCerdito(context: Context) {

    val prefs = remember {
        context.getSharedPreferences(
            "don_cerdito_caja",
            Context.MODE_PRIVATE
        )
    }

    val productos = remember {
        mutableStateListOf<Producto>().apply {
            val j = JSONArray(prefs.getString("productos", "[]"))
            for (i in 0 until j.length()) {
                val p = j.getJSONObject(i)
                add(
                    Producto(
                        p.getLong("id"),
                        p.getString("nombre"),
                        p.getDouble("costo"),
                        p.getDouble("precio"),
                        p.getDouble("stock")
                    )
                )
            }
        }
    }

    val ventas = remember {
        mutableStateListOf<Venta>().apply {
            val j = JSONArray(prefs.getString("ventas", "[]"))
            for (i in 0 until j.length()) {
                val v = j.getJSONObject(i)
                add(
                    Venta(
                        v.getLong("id"),
                        v.getLong("productoId"),
                        v.getString("producto"),
                        v.getDouble("libras"),
                        v.getDouble("total"),
                        v.getBoolean("credito"),
                        v.getString("cliente")
                    )
                )
            }
        }
    }

    val gastos = remember {
        mutableStateListOf<Gasto>().apply {
            val j = JSONArray(prefs.getString("gastos", "[]"))
            for (i in 0 until j.length()) {
                val g = j.getJSONObject(i)
                add(
                    Gasto(
                        g.getLong("id"),
                        g.getString("concepto"),
                        g.getDouble("valor")
                    )
                )
            }
        }
    }

    val creditos = remember {
        mutableStateListOf<Credito>().apply {
            val j = JSONArray(prefs.getString("creditos", "[]"))
            for (i in 0 until j.length()) {
                val c = j.getJSONObject(i)
                add(
                    Credito(
                        c.getLong("id"),
                        c.getString("cliente"),
                        c.getDouble("saldo")
                    )
                )
            }
        }
    }

    var pantalla by remember { mutableStateOf("Inicio") }
    var nombre by remember { mutableStateOf("") }
    var costo by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var libras by remember { mutableStateOf("") }
    var total by remember { mutableStateOf("") }
    var seleccionado by remember { mutableStateOf(0L) }
    var cliente by remember { mutableStateOf("") }
    var concepto by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var fondo by remember {
        mutableStateOf(
            prefs.getFloat("fondo", 0f).toDouble()
        )
    }
    var abonos by remember {
        mutableStateOf(
            prefs.getFloat("abonos", 0f).toDouble()
        )
    }
    var aviso by remember { mutableStateOf("") }
    var esCredito by remember { mutableStateOf(false) }

    fun guardar() {
        val jp = JSONArray()
        productos.forEach {
            jp.put(JSONObject().apply {
                put("id", it.id)
                put("nombre", it.nombre)
                put("costo", it.costo)
                put("precio", it.precio)
                put("stock", it.stock)
            })
        }

        val jv = JSONArray()
        ventas.forEach {
            jv.put(JSONObject().apply {
                put("id", it.id)
                put("productoId", it.productoId)
                put("producto", it.producto)
                put("libras", it.libras)
                put("total", it.total)
                put("credito", it.credito)
                put("cliente", it.cliente)
            })
        }

        val jg = JSONArray()
        gastos.forEach {
            jg.put(JSONObject().apply {
                put("id", it.id)
                put("concepto", it.concepto)
                put("valor", it.valor)
            })
        }

        val jc = JSONArray()
        creditos.forEach {
            jc.put(JSONObject().apply {
                put("id", it.id)
                put("cliente", it.cliente)
                put("saldo", it.saldo)
            })
        }

        prefs.edit()
            .putString("productos", jp.toString())
            .putString("ventas", jv.toString())
            .putString("gastos", jg.toString())
            .putString("creditos", jc.toString())
            .putFloat("fondo", fondo.toFloat())
            .putFloat("abonos", abonos.toFloat())
            .apply()
    }

    fun agregarCredito(nombreCliente: String, monto: Double) {
        val indice = creditos.indexOfFirst {
            it.cliente.equals(nombreCliente, true)
        }

        if (indice >= 0) {
            val c = creditos[indice]
            creditos[indice] = c.copy(
                saldo = c.saldo + monto
            )
        } else {
            creditos.add(
                Credito(
                    System.currentTimeMillis(),
                    nombreCliente,
                    monto
                )
            )
        }
    }

    val ventasEfectivo = ventas
        .filter { !it.credito }
        .sumOf { it.total }

    val ventasCredito = ventas
        .filter { it.credito }
        .sumOf { it.total }

    val gastosTotal = gastos.sumOf { it.valor }

    val efectivoEsperado =
        fondo + ventasEfectivo + abonos - gastosTotal

    val deudaTotal = creditos.sumOf { it.saldo }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            "🐷 DON CERDITO",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Sistema de carnicería")

        Spacer(Modifier.height(12.dp))

        val opciones = listOf(
            "Inicio", "Inventario", "Ventas",
            "Gastos", "Fiados"
        )

        opciones.chunked(3).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                fila.forEach { opcion ->
                    Button(
                        onClick = {
                            pantalla = opcion
                            aviso = ""
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(5.dp)
                    ) {
                        Text(opcion)
                    }
                }
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        when (pantalla) {

            "Inicio" -> {
                Text(
                    "RESUMEN DE CAJA",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(12.dp))

                Text("Ventas en efectivo: $${"%.2f".format(ventasEfectivo)}")
                Text("Ventas a crédito: $${"%.2f".format(ventasCredito)}")
                Text("Abonos recibidos: $${"%.2f".format(abonos)}")
                Text("Gastos: $${"%.2f".format(gastosTotal)}")
                Text("Deudas pendientes: $${"%.2f".format(deudaTotal)}")

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                Text(
                    "EFECTIVO ESPERADO",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    "$${"%.2f".format(efectivoEsperado)}",
                    style = MaterialTheme.typography.headlineMedium
                )

                OutlinedTextField(
                    value = if (fondo == 0.0) "" else fondo.toString(),
                    onValueChange = {
                        fondo = it.toDoubleOrNull() ?: 0.0
                        guardar()
                    },
                    label = { Text("Fondo inicial de caja") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            "Inventario" -> {
                Text(
                    "INVENTARIO Y COMPRAS",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    nombre,
                    { nombre = it },
                    label = { Text("Producto o corte") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    costo,
                    { costo = it },
                    label = { Text("Costo por libra") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    precio,
                    { precio = it },
                    label = { Text("Precio de venta por libra") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    libras,
                    { libras = it },
                    label = { Text("Libras compradas o ingresadas") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val c = costo.toDoubleOrNull()
                        val p = precio.toDoubleOrNull()
                        val l = libras.toDoubleOrNull()

                        if (nombre.isNotBlank() &&
                            c != null && p != null && l != null &&
                            c >= 0 && p >= 0 && l > 0
                        ) {
                            val indice = productos.indexOfFirst {
                                it.nombre.equals(nombre.trim(), true)
                            }

                            if (indice >= 0) {
                                val anterior = productos[indice]
                                val nuevoStock = anterior.stock + l
                                val nuevoCosto =
                                    ((anterior.stock * anterior.costo) +
                                     (l * c)) / nuevoStock

                                productos[indice] = anterior.copy(
                                    costo = nuevoCosto,
                                    precio = p,
                                    stock = nuevoStock
                                )
                            } else {
                                productos.add(
                                    Producto(
                                        System.currentTimeMillis(),
                                        nombre.trim(),
                                        c,
                                        p,
                                        l
                                    )
                                )
                            }

                            guardar()
                            nombre = ""
                            costo = ""
                            precio = ""
                            libras = ""
                            aviso = "Compra registrada correctamente"
                        } else {
                            aviso = "Revisa los datos ingresados"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar compra")
                }

                Text(aviso)

                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                productos.toList().forEach { producto ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                producto.nombre,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Stock: ${producto.stock} lb")
                            Text("Costo: $${producto.costo} / lb")
                            Text("Venta: $${producto.precio} / lb")
                            Text(
                                "Valor de costo: $${producto.stock * producto.costo}"
                            )

                            TextButton(onClick = {
                                productos.remove(producto)
                                guardar()
                                aviso = "Producto eliminado"
                            }) {
                                Text("Eliminar producto")
                            }
                        }
                    }
                }
            }

            "Ventas" -> {
                Text(
                    "REGISTRAR VENTA",
                    style = MaterialTheme.typography.titleLarge
                )

                if (productos.isEmpty()) {
                    Text("Primero registra productos en Inventario.")
                } else {
                    productos.forEach { producto ->
                        Row {
                            RadioButton(
                                selected = seleccionado == producto.id,
                                onClick = { seleccionado = producto.id }
                            )
                            Column {
                                Text(producto.nombre)
                                Text(
                                    "${producto.stock} lb disponibles - " +
                                    "$${producto.precio}/lb"
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        libras,
                        { libras = it },
                        label = { Text("Libras vendidas") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        total,
                        { total = it },
                        label = { Text("Total de la venta en dólares") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        cliente,
                        { cliente = it },
                        label = { Text("Nombre del cliente (si aplica)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row {
                        Checkbox(
                            checked = esCredito,
                            onCheckedChange = { esCredito = it }
                        )
                        Text("Venta a crédito")
                    }

                    Button(
                        onClick = {
                            val cantidad = libras.toDoubleOrNull()
                            val monto = total.toDoubleOrNull()
                            val indice = productos.indexOfFirst {
                                it.id == seleccionado
                            }

                            if (indice >= 0 &&
                                cantidad != null && monto != null &&
                                cantidad > 0 && monto > 0 &&
                                cantidad <= productos[indice].stock &&
                                (!esCredito || cliente.isNotBlank())
                            ) {
                                val p = productos[indice]

                                productos[indice] = p.copy(
                                    stock = p.stock - cantidad
                                )

                                ventas.add(
                                    Venta(
                                        System.currentTimeMillis(),
                                        p.id,
                                        p.nombre,
                                        cantidad,
                                        monto,
                                        esCredito,
                                        cliente.trim()
                                    )
                                )

                                if (esCredito) {
                                    agregarCredito(cliente.trim(), monto)
                                }

                                guardar()
                                libras = ""
                                total = ""
                                cliente = ""
                                esCredito = false
                                aviso = "Venta registrada correctamente"
                            } else {
                                aviso = "Revisa producto, libras, total y cliente"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar venta")
                    }
                }

                Text(aviso)

                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                Text("HISTORIAL DE VENTAS")

                ventas.toList().asReversed().forEach { venta ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                venta.producto,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("${venta.libras} lb")
                            Text("Total: $${venta.total}")
                            Text(
                                if (venta.credito)
                                    "Fiado: ${venta.cliente}"
                                else "Pago en efectivo"
                            )

                            TextButton(onClick = {
                                val indice = productos.indexOfFirst {
                                    it.id == venta.productoId
                                }

                                if (indice >= 0) {
                                    val p = productos[indice]
                                    productos[indice] = p.copy(
                                        stock = p.stock + venta.libras
                                    )
                                }

                                if (venta.credito) {
                                    val ci = creditos.indexOfFirst {
                                        it.cliente.equals(
                                            venta.cliente,
                                            true
                                        )
                                    }

                                    if (ci >= 0) {
                                        val c = creditos[ci]
                                        val saldo = (c.saldo - venta.total)
                                            .coerceAtLeast(0.0)

                                        if (saldo == 0.0) {
                                            creditos.removeAt(ci)
                                        } else {
                                            creditos[ci] = c.copy(
                                                saldo = saldo
                                            )
                                        }
                                    }
                                }

                                ventas.remove(venta)
                                guardar()
                                aviso = "Venta eliminada y stock devuelto"
                            }) {
                                Text("Eliminar venta")
                            }
                        }
                    }
                }
            }

            "Gastos" -> {
                Text(
                    "GASTOS DEL NEGOCIO",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    concepto,
                    { concepto = it },
                    label = { Text("Concepto del gasto") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    valor,
                    { valor = it },
                    label = { Text("Valor en dólares") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val v = valor.toDoubleOrNull()

                        if (concepto.isNotBlank() && v != null && v > 0) {
                            gastos.add(
                                Gasto(
                                    System.currentTimeMillis(),
                                    concepto.trim(),
                                    v
                                )
                            )
                            guardar()
                            concepto = ""
                            valor = ""
                            aviso = "Gasto registrado"
                        } else {
                            aviso = "Revisa los datos"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar gasto")
                }

                Text(aviso)

                gastos.toList().asReversed().forEach { gasto ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(gasto.concepto)
                            Text("$${gasto.valor}")

                            TextButton(onClick = {
                                gastos.remove(gasto)
                                guardar()
                                aviso = "Gasto eliminado"
                            }) {
                                Text("Eliminar gasto")
                            }
                        }
                    }
                }
            }

            "Fiados" -> {
                Text(
                    "CLIENTES Y CRÉDITOS",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    cliente,
                    { cliente = it },
                    label = { Text("Nombre del cliente") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    valor,
                    { valor = it },
                    label = { Text("Monto del nuevo fiado") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val v = valor.toDoubleOrNull()

                        if (cliente.isNotBlank() && v != null && v > 0) {
                            agregarCredito(cliente.trim(), v)
                            guardar()
                            cliente = ""
                            valor = ""
                            aviso = "Crédito registrado"
                        } else {
                            aviso = "Revisa los datos"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar fiado")
                }

                Text(aviso)

                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                creditos.toList().forEach { credito ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                credito.cliente,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Debe: $${credito.saldo}")

                            TextButton(onClick = {
                                seleccionado = credito.id
                                cliente = credito.cliente
                                valor = ""
                            }) {
                                Text("Seleccionar para abono")
                            }

                            if (seleccionado == credito.id) {
                                OutlinedTextField(
                                    value = valor,
                                    onValueChange = { valor = it },
                                    label = { Text("Cantidad del abono") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(onClick = {
                                    val monto = valor.toDoubleOrNull()

                                    if (monto != null &&
                                        monto > 0 &&
                                        monto <= credito.saldo
                                    ) {
                                        val nuevoSaldo =
                                            credito.saldo - monto

                                        if (nuevoSaldo < 0.005) {
                                            creditos.remove(credito)
                                        } else {
                                            val ci = creditos.indexOfFirst {
                                                it.id == credito.id
                                            }

                                            if (ci >= 0) {
                                                creditos[ci] = credito.copy(
                                                    saldo = nuevoSaldo
                                                )
                                            }
                                        }

                                        abonos += monto
                                        guardar()
                                        valor = ""
                                        cliente = ""
                                        seleccionado = 0L
                                        aviso = "Abono registrado"
                                    } else {
                                        aviso = "Abono inválido"
                                    }
                                }) {
                                    Text("Guardar abono")
                                }
                            }

                            TextButton(onClick = {
                                creditos.remove(credito)
                                guardar()
                                aviso = "Crédito eliminado"
                            }) {
                                Text("Eliminar crédito")
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "DON CERDITO - Datos guardados en este teléfono",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
