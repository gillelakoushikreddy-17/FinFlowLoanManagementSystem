# 📘 FinFlow Loan Management System — Complete Viva Guide
### Prepared for: Beginner | Core Java Background | Exam Tomorrow

---

## 1. 🏦 PROJECT OVERVIEW

### What is this project?
**FinFlow** is an online **Loan Management System** — it's a backend application that lets people apply for loans and lets admins approve or reject those applications.

### What problem does it solve?
Normally, if you want a loan from a bank:
- You walk in → fill papers → wait days → admin manually checks → you get a call.

FinFlow automates this entire process digitally:
- User registers → logs in → fills online form → uploads documents → admin reviews → approves/rejects automatically.

### Real-world example
> Think of it like **HDFC Bank's loan portal** or **Bajaj Finserv's app** — you apply online, upload Aadhaar/PAN, and the bank's system processes it automatically.

---

## 2. 🏗️ PROJECT ARCHITECTURE

### Architecture Style: **Microservices Architecture**
This project does NOT use one big server. Instead it uses **6 small, independent servers** — each with its own job.

```
                  ┌─────────────────┐
     USER ──────► │   API Gateway   │ (Port 8080) — Single entry point
                  └────────┬────────┘
                           │ routes to...
          ┌────────────────┼───────────────────┐
          ▼                ▼                   ▼                ▼
   ┌─────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
   │ Auth Service│  │  Application │  │   Document   │  │    Admin     │
   │  (Login/    │  │   Service    │  │   Service    │  │   Service    │
   │  Register)  │  │ (Loan Forms) │  │ (File Upload)│  │  (Decisions) │
   └──────┬──────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
          │                │                  │                  │
          ▼                ▼                  ▼                  ▼
      finflow_auth   finflow_application  finflow_document  finflow_admin
       (MySQL DB)       (MySQL DB)          (MySQL DB)       (MySQL DB)

                  ┌─────────────────┐
                  │ Service Registry│ (Port 8761) — All services register here
                  │    (Eureka)     │
                  └─────────────────┘

                  ┌─────────────────┐
                  │    RabbitMQ     │ — Message queue for async communication
                  └─────────────────┘
```

### How a request travels (simple example: User applies for loan)
```
1. User sends request → API Gateway (:8080)
2. Gateway checks JWT token (is user logged in?)
3. Gateway forwards to Application Service
4. Application Service Controller receives it
5. Controller calls Service (business logic)
6. Service calls Repository (database layer)
7. Repository talks to MySQL → saves data
8. Response goes back: Repository → Service → Controller → Gateway → User
```

### Within each service — Layered Architecture (MVC-style):
```
Controller (handles HTTP) → Service (business logic) → Repository (database) → MySQL
```

---

## 3. 📁 FOLDER STRUCTURE

Each microservice follows the same package structure. Let's take `auth-service` as example:

```
auth-service/
└── src/main/java/com/finflow/auth_service/
    ├── controller/          ← Handles incoming HTTP requests
    ├── service/             ← Contains business logic
    ├── repository/          ← Talks to the database
    ├── entity/              ← Represents a database table (a Java class = a table)
    ├── dto/                 ← Data Transfer Objects (what we send/receive, not stored)
    ├── security/            ← JWT, password encoder, security rules
    └── AuthServiceApplication.java  ← Main class, starts the server
```

### What each package does:

| Package | Purpose | Real Life Analogy |
|---|---|---|
| `controller` | Receives requests from user, sends response | Bank's front desk receptionist |
| `service` | Does the actual work/logic | Bank's loan officer |
| `repository` | Fetches/saves data from DB | Bank's file room clerk |
| `entity` | Maps to a DB table | A form/paper with fixed fields |
| `dto` | What data user sends or receives | Filled application form |
| `security` | JWT token, login checks | Bank's security guard |
| `config` | Setup for RabbitMQ, RestTemplate etc. | Bank's rulebook |

---

## 4. 🔧 TECHNOLOGIES USED

| Technology | Why It's Used |
|---|---|
| **Java 17** | The programming language. Version 17 is the latest stable LTS version |
| **Spring Boot 3** | Framework that auto-configures everything — no XML config needed |
| **Spring Security** | Handles login, password checks, and token validation |
| **JWT (JSON Web Token)** | Stateless authentication — user carries a token instead of session |
| **Spring Data JPA** | Lets us write zero SQL — Java methods auto-convert to SQL |
| **Hibernate** | JPA implementation — does the actual ORM (Object-Relational Mapping) |
| **MySQL** | Relational database to store users, loans, documents, decisions |
| **RabbitMQ** | Message queue — when admin approves loan, it sends an async message to update status |
| **Eureka (Netflix)** | Service Discovery — all services register themselves, so they can find each other |
| **API Gateway** | Single entry point — routes all requests to the right service |
| **Maven** | Build tool — downloads dependencies (libraries) and builds the project |
| **Docker** | Packages the entire app into containers so it runs anywhere |
| **Zipkin** | Distributed tracing — shows logs across all services in one place |

---

## 5. 💻 CODE EXPLANATION (LINE BY LINE)

---

### 📌 5A. AuthServiceApplication.java (Main Class)
```java
@SpringBootApplication          // ← Magic annotation: starts Spring Boot, enables everything
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);  // ← Starts the server
    }
}
```
> Think of this as pressing the "ON" button for the auth service.

---

### 📌 5B. User.java (Entity — maps to `users` table in MySQL)
```java
@Entity                          // ← "This class = a table in DB"
@Table(name = "users")           // ← Table name in MySQL is "users"
public class User {

    @Id                          // ← This field is the Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // ← Auto-increment (1, 2, 3...)
    private Long id;

    private String name;         // ← Column "name" in users table
    private String email;        // ← Column "email"
    private String password;     // ← Stored as BCrypt hash, NEVER plain text
    private String role;         // ← "APPLICANT" or "ADMIN"

    // Getters and Setters below — allow reading/writing private fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // ... etc
}
```
> Think of `User` like a row in an Excel sheet. Each field = a column.

---

### 📌 5C. UserRepository.java (Repository — talks to DB)
```java
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository gives us FREE methods:
    // save(), findAll(), findById(), deleteById() — no SQL needed!

    Optional<User> findByEmail(String email);
    // ↑ Spring auto-generates SQL: SELECT * FROM users WHERE email = ?
    // We didn't write any SQL at all!
}
```
> It's like asking a robot: "Find me a user by email" — the robot writes the SQL automatically.

---

### 📌 5D. AuthService.java (Service — business logic)
```java
@Service    // ← Tells Spring: "This is a service class, manage it for me"
public class AuthService {

    @Autowired                   // ← Spring automatically injects (creates) these objects
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;   // ← For hashing passwords

    @Autowired
    private JwtUtil jwtUtil;                   // ← For creating JWT tokens

    public User saveUser(User user) {
        // NEVER store plain text password!
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // encode("tiger") → "$2a$10$xyz..." (BCrypt hash)

        if(user.getRole() == null || user.getRole().isEmpty()){
            user.setRole("APPLICANT");   // Default role if not provided
        }
        return repository.save(user);    // Save to MySQL
    }

    public String generateToken(String email) {
        Optional<User> userOptional = repository.findByEmail(email);
        if(userOptional.isPresent()){
            User user = userOptional.get();
            return jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
            // Creates a JWT token like: "eyJhbGciOiJIUzI1NiJ9.eyJ..."
        }
        throw new RuntimeException("User not found");
    }

    public void validateToken(String token) {
        jwtUtil.validateToken(token);    // Throws exception if token is invalid/expired
    }
}
```

---

### 📌 5E. AuthController.java (Controller — handles HTTP requests)
```java
@RestController     // ← "This class handles REST API requests and returns JSON"
@RequestMapping("") // ← Base URL path (empty = root)
public class AuthController {

    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager authenticationManager;  // ← Spring's login manager

    @Autowired
    private UserRepository userRepository;

    // POST /signup — Register a new user
    @PostMapping("/signup")
    public ResponseEntity<?> addNewUser(@RequestBody User user) {
        // @RequestBody: reads JSON from HTTP request body and converts to User object
        try {
            if(userRepository.findByEmail(user.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Email already exists");
                // Returns HTTP 400 if email is taken
            }
            User savedUser = service.saveUser(user);
            return ResponseEntity.ok(savedUser);    // Returns HTTP 200 with saved user
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST /login — Login and get JWT token
    @PostMapping("/login")
    public ResponseEntity<?> getToken(@RequestBody AuthRequest authRequest) {
        // Step 1: Verify email + password using Spring Security
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );
        if (authenticate.isAuthenticated()) {
            // Step 2: Generate JWT token
            String token = service.generateToken(authRequest.getEmail());
            User user = userRepository.findByEmail(authRequest.getEmail()).orElseThrow();
            // Step 3: Return token + userId + role
            return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getRole()));
        } else {
            return ResponseEntity.status(401).body("Invalid access");
        }
    }

    // GET /validate?token=xxx — Check if token is valid
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestParam("token") String token) {
        service.validateToken(token);
        return ResponseEntity.ok("Token is valid");
    }
}
```

---

### 📌 5F. JwtUtil.java (Security — creates and validates JWT tokens)
```java
@Component   // ← Tells Spring: "This is a helper class, manage it"
public class JwtUtil {

    @Value("${jwt.secret}")       // ← Reads value from application.yml
    private String secret;        // ← Secret key used to sign tokens

    @Value("${jwt.expiration}")
    private long expiration;      // ← Token expires in 86400000 ms = 24 hours

    public String generateToken(String userName, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);         // Adds role inside token
        claims.put("userId", userId);     // Adds userId inside token
        return createToken(claims, userName);
    }

    private String createToken(Map<String, Object> claims, String userName) {
        return Jwts.builder()
                .setClaims(claims)                    // ← Data inside the token
                .setSubject(userName)                 // ← Who this token belongs to
                .setIssuedAt(new Date(...))           // ← When created
                .setExpiration(new Date(... + expiration))  // ← When it expires
                .signWith(getSignKey(), HS256)        // ← Sign with secret key
                .compact();                           // ← Build the final token string
    }

    public void validateToken(String token) {
        // If token is invalid or expired, this throws an exception automatically
        Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }
}
```
> JWT is like a **hotel room key card** — you get it at check-in (login), carry it everywhere, and the door (API) lets you in automatically without asking your name again.

---

### 📌 5G. SecurityConfig.java (Configuration — security rules)
```java
@Configuration      // ← "This class contains Spring beans/settings"
@EnableWebSecurity  // ← "Enable Spring Security"
public class SecurityConfig {

    @Bean   // ← "Create this object and add it to Spring's container"
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())   // ← Disable CSRF (not needed for REST APIs)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/signup", "/login", "/validate").permitAll()
                // ↑ These URLs are PUBLIC — no login needed
                .anyRequest().authenticated()
                // ↑ Everything else requires authentication
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(STATELESS));
            // ↑ No sessions — we use JWT tokens instead

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // ← BCrypt is a strong hashing algorithm
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();  // ← Handles login verification
    }
}
```

---

### 📌 5H. LoanApplication.java (Application Service Entity)
```java
@Entity
@Table(name = "loan_applications")
public class LoanApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long applicantId;       // ← Which user applied
    private String personalDetails; // ← Name, address etc (stored as text/JSON string)
    private String employmentDetails; // ← Job info
    private BigDecimal loanAmount;  // ← How much loan (e.g., 500000.00)
    private Integer loanTermMonths; // ← For how many months (e.g., 24)
    private String status;          // ← DRAFT → SUBMITTED → APPROVED/REJECTED
}
```
**Loan Application Lifecycle:**
```
DRAFT (filling form) → SUBMITTED (sent to bank) → APPROVED or REJECTED
```

---

### 📌 5I. ApplicationService.java (Application Service — business logic)
```java
@Service
public class ApplicationService {

    @Autowired
    private LoanApplicationRepository repository;

    // Create a new loan application as DRAFT
    @CacheEvict(value = "applications", allEntries = true)
    // ↑ Clears cache so next fetch gets fresh data
    public LoanApplication createDraft(Long applicantId, LoanApplication application) {
        application.setApplicantId(applicantId);
        application.setStatus("DRAFT");      // Always starts as DRAFT
        return repository.save(application);
    }

    // Update draft (only DRAFT can be edited)
    public LoanApplication updateApplication(Long id, LoanApplication updated) {
        LoanApplication existing = repository.findById(id).orElseThrow(...);
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new RuntimeException("Cannot update submitted application");
            // ← Once submitted, you can't change it (like a submitted exam paper)
        }
        // Update fields
        existing.setPersonalDetails(updated.getPersonalDetails());
        // ... etc
        return repository.save(existing);
    }

    // Submit (changes status from DRAFT to SUBMITTED)
    public LoanApplication submitApplication(Long id) {
        LoanApplication existing = repository.findById(id).orElseThrow(...);
        existing.setStatus("SUBMITTED");
        return repository.save(existing);
    }

    // Called by RabbitMQ listener when admin makes decision
    public LoanApplication updateStatus(Long id, String newStatus) {
        LoanApplication existing = repository.findById(id).orElseThrow(...);
        existing.setStatus(newStatus.toUpperCase());  // APPROVED or REJECTED
        return repository.save(existing);
    }

    @Cacheable(value = "applications", key = "'all'")
    // ↑ Cache result so we don't hit DB every time
    public List<LoanApplication> getAllApplications() {
        return repository.findAll();
    }
}
```

---

### 📌 5J. StatusUpdateListener.java (RabbitMQ Message Listener)
```java
@Service
public class StatusUpdateListener {

    @Autowired
    private ApplicationService applicationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    // ↑ "Listen to this queue — when a message arrives, run this method"
    public void handleStatusUpdate(StatusUpdateMessage message) {
        System.out.println("Received status update for application " + message.getApplicationId());
        applicationService.updateStatus(message.getApplicationId(), message.getStatus());
        // ← Updates loan status in DB when admin approves/rejects
    }
}
```
**How RabbitMQ works in this project:**
```
Admin approves loan
        ↓
AdminService sends message to RabbitMQ queue
        ↓
RabbitMQ delivers message to ApplicationService
        ↓
StatusUpdateListener receives it
        ↓
Updates loan status to APPROVED/REJECTED in DB
```
> RabbitMQ is like a **postal service** — Admin puts a letter (message) in the mailbox (queue), and Application Service picks it up when ready. They don't need to talk directly.

---

### 📌 5K. AdminService.java (Admin — decisions and reports)
```java
@Service
public class AdminService {

    @Autowired
    private RestTemplate restTemplate;  // ← Used to call other microservices via HTTP

    @Autowired
    private RabbitTemplate rabbitTemplate;  // ← Used to send RabbitMQ messages

    // Calls application-service to get all loan applications
    @CircuitBreaker(name = "applicationService", fallbackMethod = "fallbackGetAllApplications")
    public Object[] getAllApplications() {
        return restTemplate.getForObject("http://application-service/all", Object[].class);
        // ↑ Calls application-service's /all endpoint via Eureka service discovery
    }

    // If application-service is down, return empty list (don't crash!)
    public Object[] fallbackGetAllApplications(Throwable t) {
        return new Object[]{};   // ← Graceful failure
    }

    // Admin approves or rejects a loan
    public Decision makeDecision(Long applicationId, Long adminId, String decisionType, String remarks) {
        Decision decision = new Decision(...);

        // Send message to RabbitMQ
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, msg);
        // ↑ This message goes to application-service to update loan status

        return decisionRepository.save(decision);   // Save decision in admin's DB
    }
}
```

---

### 📌 5L. DocumentController.java (Document Service — file uploads)
```java
@RestController
public class DocumentController {

    private final String UPLOAD_DIR = "C:/temp/uploads/";

    // Upload a document (Aadhaar/PAN/Income Certificate)
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadDocument(
            @RequestHeader("X-User-Id") Long applicantId,   // ← Sent by gateway
            @RequestParam("applicationId") Long applicationId,
            @RequestParam("type") DocumentType type,         // ← Enum (dropdown)
            @RequestParam("file") MultipartFile file) {      // ← Actual file

        // 1. Create folder if doesn't exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        // 2. Save file with timestamp prefix to avoid duplicate names
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String filePath = UPLOAD_DIR + fileName;
        file.transferTo(new File(filePath));   // ← Saves file to disk

        // 3. Save metadata (not file itself) to DB
        Document document = service.uploadDocument(applicantId, applicationId, type, ...);

        // 4. Check if all 3 docs are uploaded
        boolean allComplete = service.areAllDocumentsUploaded(applicationId);
        if (allComplete) {
            return ResponseEntity.ok("All 3 required documents uploaded!");
        }
        // Tell user how many docs are still missing
        return ResponseEntity.ok(missing + " more document(s) still required");
    }

    // Download/view a document
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        Document document = service.getDocumentById(id);
        File file = new File(document.getFilePath());   // ← Gets file from disk

        // Detect file type and set correct content type
        String contentType = "application/pdf";  // PDFs open inline in browser
        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header("Content-Disposition", "inline; filename=" + document.getFileName())
                .body(resource);   // ← Streams the actual file to user
    }
}
```

---

## 6. 📝 ANNOTATIONS EXPLAINED

| Annotation | Where Used | What It Does | Simple Analogy |
|---|---|---|---|
| `@SpringBootApplication` | Main class | Starts everything: component scan, auto-config, Spring Boot | "Turn on all the machines" |
| `@RestController` | Controller | Handles HTTP requests, returns JSON automatically | "I am a REST API endpoint handler" |
| `@Service` | Service class | Marks as business logic class, Spring manages it | "I am a business logic worker" |
| `@Repository` | Repository (implied by JpaRepository) | Marks as data access class | "I talk to the database" |
| `@Component` | JwtUtil | Generic Spring-managed bean | "Spring, please manage this object" |
| `@Configuration` | SecurityConfig | Contains setup/config beans | "I define how things are configured" |
| `@Autowired` | Any field | Auto-injects (creates) the object — no `new` keyword needed | "Spring, give me one of these automatically" |
| `@Entity` | User, LoanApplication | This Java class maps to a DB table | "I am a database table" |
| `@Table(name="users")` | Entity class | Specify the exact table name | "My table is named 'users'" |
| `@Id` | Entity field | This field is the primary key | "This is the unique ID column" |
| `@GeneratedValue` | Id field | Auto-increment the ID | "DB auto-assign the number" |
| `@GetMapping("/path")` | Controller method | Handle HTTP GET request | "When someone does GET /path, call me" |
| `@PostMapping("/path")` | Controller method | Handle HTTP POST request | "When someone does POST /path, call me" |
| `@PutMapping("/path")` | Controller method | Handle HTTP PUT request | "When someone does PUT /path, call me" |
| `@RequestBody` | Method parameter | Read JSON from request body | "Convert incoming JSON to Java object" |
| `@RequestParam` | Method parameter | Read URL query parameter (`?name=John`) | "Get the value from URL params" |
| `@PathVariable` | Method parameter | Read value from URL path (`/{id}`) | "Get the `{id}` value from the URL" |
| `@RequestHeader` | Method parameter | Read a HTTP header value | "Read this value from request header" |
| `@Value("${key}")` | Field | Read value from application.yml | "Get config value from properties file" |
| `@Bean` | Method in Config | Creates an object and registers in Spring | "Create this and put it in Spring's bag" |
| `@RabbitListener` | Listener method | Listen to a RabbitMQ queue | "When a message arrives in queue, call me" |
| `@Cacheable` | Service method | Cache the return value (skip DB next time) | "Remember my answer, don't ask DB again" |
| `@CacheEvict` | Service method | Clear the cache when data changes | "Forget old cached answer, data changed" |
| `@CircuitBreaker` | Service method | If called service is down, use fallback | "If Plan A fails, use Plan B" |
| `@EnableWebSecurity` | Config class | Enables Spring Security | "Turn on the security guard" |

---

## 7. 🗄️ DATABASE FLOW

### How data is saved (example: User signup)

```
1. User sends POST /gateway/auth/signup with JSON body
2. AuthController receives, converts JSON → User object (@RequestBody)
3. AuthService.saveUser() is called
4. password = BCrypt.encode("tiger") → "$2a$10$abc..." (hash)
5. repository.save(user) is called
6. JPA/Hibernate generates SQL automatically:
   INSERT INTO users (name, email, password, role)
   VALUES ('Rahul', 'rahul@gmail.com', '$2a$10$abc...', 'APPLICANT');
7. MySQL executes the SQL, assigns id=1
8. Returns saved User object → JSON response
```

### How JPA works internally
```
Java Class (User) ←──── JPA/Hibernate ────→ MySQL Table (users)

User.id         ←──────────────────────→  users.id (BIGINT PRIMARY KEY AUTO_INCREMENT)
User.name       ←──────────────────────→  users.name (VARCHAR)
User.email      ←──────────────────────→  users.email (VARCHAR)
User.password   ←──────────────────────→  users.password (VARCHAR)
User.role       ←──────────────────────→  users.role (VARCHAR)
```

JPA creates database tables **automatically** on startup because:
```yaml
jpa:
  hibernate:
    ddl-auto: update   # ← Creates/updates tables automatically
```

### SQL Queries generated by JPA (examples):
```sql
-- repository.save(user)  →
INSERT INTO users (email, name, password, role) VALUES (?, ?, ?, ?);

-- repository.findByEmail(email)  →
SELECT * FROM users WHERE email = ?;

-- repository.findById(id)  →
SELECT * FROM users WHERE id = ?;

-- repository.findAll()  →
SELECT * FROM users;
```

### Databases in this project:
| Database | Service | Tables |
|---|---|---|
| `finflow_auth` | Auth Service | `users` |
| `finflow_application` | Application Service | `loan_applications` |
| `finflow_document` | Document Service | `documents` |
| `finflow_admin` | Admin Service | `decisions`, `reports` |

---

## 8. 🌐 API EXPLANATION

All APIs are accessed through the **API Gateway at port 8080**.

---

### 🔐 Auth Service APIs (`/gateway/auth/...`)

#### 1. Register User
```
POST http://localhost:8080/gateway/auth/signup

Request Body (JSON):
{
  "name": "Rahul Sharma",
  "email": "rahul@gmail.com",
  "password": "password123",
  "role": "APPLICANT"
}

Response (200 OK):
{
  "id": 1,
  "name": "Rahul Sharma",
  "email": "rahul@gmail.com",
  "password": "$2a$10$hashedpassword",
  "role": "APPLICANT"
}
```

#### 2. Login
```
POST http://localhost:8080/gateway/auth/login

Request Body:
{
  "email": "rahul@gmail.com",
  "password": "password123"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQVBQTElDQU5UIiwidXNlcklkIjoxfQ...",
  "userId": 1,
  "role": "APPLICANT"
}
```

#### 3. Validate Token
```
GET http://localhost:8080/gateway/auth/validate?token=eyJhbG...

Response (200 OK):
"Token is valid"

Response (401):
"Invalid/Expired token"
```

---

### 📋 Application Service APIs (`/gateway/applications/...`)

#### 1. Create Draft Application
```
POST http://localhost:8080/gateway/applications
Headers: X-User-Id: 1

Request Body:
{
  "personalDetails": "Rahul Sharma, Mumbai",
  "employmentDetails": "Software Engineer at TCS",
  "loanAmount": 500000,
  "loanTermMonths": 24
}

Response (200 OK):
{
  "id": 1,
  "applicantId": 1,
  "personalDetails": "Rahul Sharma, Mumbai",
  "loanAmount": 500000.00,
  "loanTermMonths": 24,
  "status": "DRAFT"
}
```

#### 2. Submit Application
```
POST http://localhost:8080/gateway/applications/1/submit

Response (200 OK):
{
  "id": 1,
  "status": "SUBMITTED"
}
```

#### 3. Check Status
```
GET http://localhost:8080/gateway/applications/1/status

Response (200 OK):
{
  "status": "APPROVED"
}
```

---

### 📁 Document Service APIs (`/gateway/documents/...`)

#### 1. Upload Document
```
POST http://localhost:8080/gateway/documents/upload
Headers: X-User-Id: 1
Content-Type: multipart/form-data

Form fields:
  applicationId = 1
  type = AADHAAR        ← Must be: AADHAAR, PAN_CARD, or INCOME_CERTIFICATE
  file = [choose file]

Response (200 OK):
{
  "document": { "id": 1, "type": "AADHAAR", "status": "UPLOADED" },
  "message": "2 more document(s) still required"
}
```

---

### 👨‍💼 Admin Service APIs (`/gateway/admin/...`)

#### 1. Make Decision
```
POST http://localhost:8080/gateway/admin/applications/1/decision
Headers: X-User-Id: 2    ← Admin's user ID

Query Parameters:
  decisionType = APPROVED
  remarks = Good credit score

Response (200 OK):
{
  "id": 1,
  "applicationId": 1,
  "adminId": 2,
  "decisionType": "APPROVED",
  "remarks": "Good credit score",
  "decisionAt": "2026-03-26T09:00:00"
}
```

---

### HTTP Status Codes Used:

| Code | Meaning | When |
|---|---|---|
| 200 OK | Success | Request processed correctly |
| 400 Bad Request | Client error | Wrong input, email already exists etc. |
| 401 Unauthorized | Not logged in | Invalid/missing token |
| 403 Forbidden | Not allowed | Logged in but don't have permission |
| 404 Not Found | Data missing | Application/document not found |
| 500 Internal Server Error | Server crashed | Unexpected error |

---

## 9. 🚀 DEPLOYMENT

### How it's deployed (using Docker):

```
Developer's Code
      ↓
Maven builds → JAR file (executable)
      ↓
Dockerfile packages → Docker Image
      ↓
docker-compose up → All 6 containers start simultaneously
      ↓
All on same Docker network (finflowloanmanagementsystem_finflow-net)
```

### What Docker does:
Docker packages the application + Java JRE + all dependencies into a **container** — like a self-contained box. It runs the same way on any machine.

```dockerfile
# Dockerfile (same for all services)
FROM maven:3.9-eclipse-temurin-17 AS build    # ← Use Maven image to build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests             # ← Build the JAR

FROM eclipse-temurin:17-jre                   # ← Smaller image for running
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar   # ← Copy the built JAR
ENTRYPOINT ["java", "-jar", "app.jar"]        # ← Run the JAR
```

### How users access it:
```
User Browser/Postman
       ↓
http://localhost:8080  (API Gateway)
       ↓
Gateway routes to correct service
       ↓
Service processes and returns JSON
       ↓
User sees the response
```

### Services accessible after deployment:
| Service | URL |
|---|---|
| API Gateway (main entry) | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| RabbitMQ Dashboard | http://localhost:15672 |
| Zipkin Tracing | http://localhost:9411 |

---

## 10. 🎓 30 MOST IMPORTANT VIVA QUESTIONS & ANSWERS

---

**Q1. What is FinFlow? What does it do?**
> FinFlow is an online Loan Management System. It allows users to register, apply for loans, upload documents (Aadhaar, PAN, Income Certificate), and admins to approve or reject those applications.

---

**Q2. What architecture does this project follow?**
> Microservices Architecture. The project has 6 independent services: Service Registry, API Gateway, Auth Service, Application Service, Document Service, and Admin Service. Each runs on its own server and has its own database.

---

**Q3. What is Microservices Architecture? Why use it?**
> Instead of one big application (Monolith), we split into small independent services. Each can be deployed, scaled, and updated independently. If one crashes, others still work.

---

**Q4. What is an API Gateway? Why is it needed?**
> API Gateway is the single entry point for all requests. Instead of users knowing 6 different ports, they only talk to port 8080. The gateway routes the request to the correct service. It also handles load balancing and circuit breaking.

---

**Q5. What is JWT? Why is it used?**
> JWT (JSON Web Token) is a secure string given to user after login. It contains user info (email, role, userId) digitally signed with a secret key. The user sends this token in every request, so the server doesn't need to store sessions (stateless authentication).

---

**Q6. What is Spring Security? How is it configured?**
> Spring Security is a framework that handles authentication and authorization. In SecurityConfig.java, we define which URLs are public (/signup, /login) and which require login. We use STATELESS sessions (JWT-based) and BCrypt for password encoding.

---

**Q7. What is BCrypt? Why don't we store plain passwords?**
> BCrypt is a one-way hashing algorithm. Passwords are never stored plain — if the DB is hacked, passwords are still safe. BCrypt.encode("tiger") → "$2a$10$abc..." (a hash you can't reverse).

---

**Q8. What is @Entity and @Table?**
> `@Entity` tells Spring/JPA that this Java class maps to a database table. `@Table(name="users")` specifies the exact table name in MySQL.

---

**Q9. What is JPA? What is Hibernate?**
> JPA (Java Persistence API) is a specification/interface for ORM. Hibernate is the implementation — it does the actual work. When you call `repository.save(user)`, Hibernate generates `INSERT INTO users...` SQL automatically.

---

**Q10. What does `extends JpaRepository<User, Long>` mean?**
> It gives the UserRepository free CRUD methods: `save()`, `findAll()`, `findById()`, `deleteById()` etc. — without writing any SQL. `User` is the entity type, `Long` is the ID type.

---

**Q11. How does `findByEmail(String email)` work without SQL?**
> Spring Data JPA reads the method name and auto-generates SQL: `SELECT * FROM users WHERE email = ?`. This is called Query Derivation.

---

**Q12. What is @Autowired?**
> It tells Spring to automatically inject (create and provide) an object. Instead of writing `UserRepository repo = new UserRepository()`, we just write `@Autowired UserRepository repo` and Spring handles creation.

---

**Q13. What is the difference between @Component, @Service, @Repository, @Controller?**
> All are Spring-managed beans (subclasses of @Component). The difference is semantic — @Service = business logic, @Repository = data access, @Controller = HTTP handling, @Component = generic utility. Spring treats all the same internally.

---

**Q14. What is RabbitMQ? How is it used here?**
> RabbitMQ is a message queue. When admin approves/rejects a loan, AdminService sends a message to RabbitMQ. ApplicationService's StatusUpdateListener receives it and updates the loan status. They communicate asynchronously without direct HTTP calls.

---

**Q15. What is Eureka Service Registry?**
> Eureka is a service discovery server. Every microservice registers itself here with its name and IP. When AdminService wants to call ApplicationService, it asks Eureka "where is application-service?" and Eureka returns the address.

---

**Q16. What is @RestController vs @Controller?**
> `@Controller` is for MVC (returns HTML views). `@RestController` = `@Controller` + `@ResponseBody` — returns JSON data directly, not HTML pages.

---

**Q17. What is ResponseEntity?**
> ResponseEntity lets you control the HTTP response — status code, headers, and body. `ResponseEntity.ok(data)` returns 200 OK with data. `ResponseEntity.badRequest().body("error")` returns 400.

---

**Q18. What is @RequestBody?**
> `@RequestBody` reads the JSON from the HTTP request body and converts it to a Java object automatically using Jackson library. E.g., `{"name":"Rahul"}` → `User` object.

---

**Q19. What is @PathVariable vs @RequestParam?**
> `@PathVariable` gets value from URL path: `/applications/{id}` → `@PathVariable Long id`. `@RequestParam` gets value from query string: `/validate?token=abc` → `@RequestParam String token`.

---

**Q20. What is Circuit Breaker? Why is it used?**
> If one service is down, Circuit Breaker prevents the whole system from crashing. In AdminService, `@CircuitBreaker(fallbackMethod="fallbackGetAllApplications")` — if application-service is unreachable, it returns an empty list instead of crashing.

---

**Q21. What is @Cacheable? Why is it used?**
> `@Cacheable` stores method results in cache. Next time the same method is called with same parameters, it returns cached result instead of hitting database. Improves performance significantly.

---

**Q22. What is ddl-auto: update in application.yml?**
> It tells Hibernate to automatically create or alter database tables based on entity classes. If a new field is added to `User` class, the `users` table gets a new column on startup.

---

**Q23. What is the loan status lifecycle?**
> DRAFT → SUBMITTED → APPROVED or REJECTED. User creates a DRAFT, fills it, submits it. Admin reviews and makes a decision which updates the status via RabbitMQ.

---

**Q24. What are the 3 required documents? How are duplicate prevention done?**
> AADHAAR, PAN_CARD, INCOME_CERTIFICATE (defined as enum DocumentType). The service checks `areAllDocumentsUploaded()` — if all 3 are present, the application is "document-complete". Using enum prevents spelling errors.

---

**Q25. How does the API Gateway know which service to route to?**
> The gateway has routes configured in application.yml. `/gateway/auth/**` goes to auth-service, `/gateway/applications/**` goes to application-service, etc. Routing uses Eureka (service name lookup, not hardcoded IP).

---

**Q26. What is MultipartFile?**
> MultipartFile is Spring's class for handling file uploads. When user uploads a PDF/JPG, it arrives as MultipartFile. We use `file.transferTo(new File(path))` to save it to disk.

---

**Q27. What is RestTemplate?**
> RestTemplate is used to make HTTP calls from one microservice to another. AdminService uses `restTemplate.getForObject("http://application-service/all", Object[].class)` to call application-service's API synchronously.

---

**Q28. What is the difference between @Bean and @Autowired?**
> `@Bean` CREATES an object and registers in Spring container (used in config classes). `@Autowired` INJECTS/uses an already created object from the Spring container.

---

**Q29. What is Maven? What is pom.xml?**
> Maven is a build tool. `pom.xml` is its configuration file listing all dependencies (libraries the project needs). Running `mvn clean package` downloads them and builds a `.jar` file.

---

**Q30. What is Docker Compose? How is it used here?**
> Docker Compose runs multiple Docker containers together using a single `docker-compose.yml` file. In this project, it starts all 6 Spring Boot services, MySQL, RabbitMQ, and Zipkin in the correct order with proper network configuration.

---

## 🎯 QUICK CHEAT SHEET

```
Project: Loan Management System (Microservices)
Language: Java 17
Framework: Spring Boot 3
Database: MySQL (4 separate DBs)
Auth: JWT + BCrypt
Communication: REST (sync) + RabbitMQ (async)
Service Discovery: Eureka
Gateway: Spring Cloud Gateway (port 8080)
Deployment: Docker + Docker Compose
Tracing: Zipkin

Services:
  service-registry  :8761  → Eureka dashboard
  api-gateway       :8080  → Entry point
  auth-service      :random → Login/Register/JWT
  application-service:random→ Loan applications
  document-service  :random → File uploads
  admin-service     :random → Decisions/Reports
```

---

*Good luck with your viva tomorrow! You've got this! 🚀*
