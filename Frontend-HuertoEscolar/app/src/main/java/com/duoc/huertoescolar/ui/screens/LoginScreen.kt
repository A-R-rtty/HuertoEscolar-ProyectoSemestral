package com.duoc.huertoescolar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    errors: Map<String, String>,
    generalError: String?,
    onLogin: (String, String) -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = "Ícono Huerto Escolar",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Huerto Escolar San Bernardo", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Gestión colaborativa de cultivos y talleres (MVP Académico)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo institucional ficticio") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                trailingIcon = {
                    if (errors.containsKey("correo")) {
                        Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                    }
                },
                isError = errors.containsKey("correo"),
                supportingText = { errors["correo"]?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    if (errors.containsKey("password")) {
                        Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                isError = errors.containsKey("password"),
                supportingText = { errors["password"]?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Animación funcional #2: aparición animada de alerta de error de autenticación
            AnimatedVisibility(visible = generalError != null) {
                Text(
                    text = generalError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onLogin(correo, password) },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Ingresar al Huerto")
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Text("Cuentas Ficticias de Prueba (1 Admin + 3 Estudiantes)", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val cuentasPrueba = listOf(
                Triple("Admin: Prof. Elena", "admin.huerto@demo.cl", "Admin1234"),
                Triple("Est. 1: Mateo (5°A)", "mateo.est@demo.cl", "Est1234"),
                Triple("Est. 2: Sofía (6°B)", "sofia.est@demo.cl", "Est1234"),
                Triple("Est. 3: Lucas (7°A)", "lucas.est@demo.cl", "Est1234")
            )

            cuentasPrueba.forEach { (etiqueta, mail, pass) ->
                OutlinedButton(
                    onClick = {
                        correo = mail
                        password = pass
                        onLogin(mail, pass)
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Text(etiqueta)
                }
            }
        }
    }
}