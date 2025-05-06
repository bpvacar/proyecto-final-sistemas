


# Meeting Manager CLI

Un sistema distribuido de gestión de reuniones corporativas, compuesto por:

- **CentralServer**: servidor central (Mediator) que recibe reuniones y notifica a cada empleado.  
- **EmployeeServer**: servidor por empleado (Observer) que recibe notificaciones y guarda un registro local.  
- **EmployeeClientApp**: cliente CLI para crear o modificar reuniones y enviarlas al CentralServer.  
- **Docker Compose**: orquesta los contenedores para central y cinco empleados (Alice, Bob, Carol, Dave, Eve).  

---

## 📋 Prerrequisitos

- Java 17+  
- Maven 3.6+  
- Docker & Docker Compose  
- (Opcional) Cuenta en GitHub y Docker Hub para versionar y publicar imágenes  

---

## 🚀 Clonar y compilar

```bash
# 1. Clona el repositorio
git clone https://github.com/<tu-usuario>/meeting-manager-cli.git
cd meeting-manager-cli

# 2. Compila con Maven
mvn clean package


Esto generará en `target/`:

* `meeting-manager-1.0.0.jar`
* (Si usas el Shade Plugin opcional) `meeting-manager-fat.jar`

---

## ⚙️ Configuración de Docker Compose

1. Crea el archivo de puertos en `./config/employees.properties`:

   ```properties
   Alice=6001
   Bob=6002
   Carol=6003
   Dave=6004
   Eve=6005
   ```

2. Crea las carpetas para datos persistentes:

   ```bash
   mkdir -p data/Alice data/Bob data/Carol data/Dave data/Eve
   ```

3. Revisa tu `docker-compose.yml` (ya incluido) que monta:

   ```yaml
   central:
     image: proyecto-final_central
     build: ./ 
     dockerfile: Dockerfile.central
     volumes:
       - ./config:/config
     ports:
       - "5001:5000"

   alice:
     image: proyecto-final_employee
     build: ./
     dockerfile: Dockerfile.employee
     environment:
       - EMP_NAME=Alice
       - EMP_PORT=6001
     ports:
       - "6001:6001"
     depends_on:
       - central
     volumes:
       - ./data/Alice:/app/data

   # ... igual para Bob, Carol, Dave, Eve
   ```

---

## 🐳 Levantar con Docker Compose

```bash
# 1. Baja cualquier stack previo
docker compose down

# 2. Reconstruye imágenes (sin cache opcional)
docker compose build --no-cache

# 3. Arranca en segundo plano
docker compose up -d
```

Verifica que todos estén “Up”:

```bash
docker compose ps
# central_1    Up  (0.0.0.0:5001->5000)
# alice_1      Up  (6001)
# bob_1        Up  (6002)
# ... etc.
```

---

## 💻 Usar el CLI de un empleado

Desde tu máquina (no dentro de Docker):

```bash
java -cp target/meeting-manager-1.0.0.jar \
  com.example.employee.EmployeeClientApp Alice localhost 5001
```

Sustituye `Alice` por cualquier empleado, y el puerto `5001` si mapeaste otro local.

### Opciones

1. **Crear reunión**
2. **Modificar reunión** (elige índice, deja en blanco para mantener campos)
3. **Salir**

---

## 📂 Ver datos persistentes

Después de crear o modificar reuniones, cada servidor de empleado guardará su fichero en `./data/<Name>/<Name>.txt`, legible en texto:

```
tema|Bob,Carol|Alice|Sala 1|2025-05-15T15:30|2025-05-15T16:00|1712345678901
```

---

## 🚢 Publicar en Docker Hub (opcional)

```bash
# 1. Login
docker login

# 2. Construir y etiquetar
docker build -f Dockerfile.central -t <usuario>/meeting-manager-central:latest .
docker build -f Dockerfile.employee -t <usuario>/meeting-manager-employee:latest .

# 3. Push
docker push <usuario>/meeting-manager-central:latest
docker push <usuario>/meeting-manager-employee:latest
```

Luego en `docker-compose.yml` reemplaza `build:` por `image: <usuario>/…:latest` y ejecuta:

```bash
docker compose pull
docker compose up -d
```

---

## 🤝 Contribuir

1. Haz un *fork*
2. Crea una rama de feature (`git checkout -b feature/nombre`)
3. Haz tus cambios y testéalo localmente
4. Empuja tu rama y abre un *Pull Request*

¡Gracias por colaborar!
