# DMIT2015 Fall 2026 Term Workbook Repository

This repository contains course work that is **not submitted for marks**, including:

- Examples completed in class
- Code from slides
- Exercises and practice activities

Clone this repository to keep track of your work and easily share code with the instructor.

# Week 1

Follow the code in the **`dmit2015-javase-demo`** folder.

# Week 2

Follow the code in the **`dmit2015-faces-demo`** folder.

## Lesson 4: Introduction to Jakarta Faces

**Coding files:**
- `HelloBean.java`
- `GreetingBean.java`
- `hello.xhtml`
- `index.xhtml`

**Topics:** Jakarta Faces, Maven WAR projects, Tomcat 11, Facelets, CDI managed beans, EL binding, and PrimeFaces.

## Lesson 5: JSF Components, Form Binding & Validation

**Coding files:**
- `StudentFormBean.java`
- `student-form.xhtml`

**Topics:** JSF form components, bean property binding, `@ViewScoped`, validation, `process="@form"`, and `update="@form"`.

### Lesson 6: Collections, Data Tables, Navigation & JSF Lifecycle

**Coding files:**

- `StudentFormBean.java`
- `RegistrationBean.java`
- `StudentInfo.java`
- `student-form.xhtml`
- `registration.xhtml`
- `registration-success.xhtml`

**Topics:** Collections and data tables, JSF navigation, action methods, implicit navigation, redirects with `faces-redirect=true`, JSF lifecycle phases, and validation behavior.

# Week 3–4

Follow the code in the **`dmit2015-faces-firebase-demo`** folder.

## Lesson 7: Architecture and Strategy Pattern

**Setup files:**
- Import/download project template from Brightspace
- `pom.xml` --added code from [https://lms.nait.ca/d2l/le/lessons/191328/topics/6251889](https://lms.nait.ca/d2l/le/lessons/191328/topics/6491233)
- `beans.xml` -- added code from https://lms.nait.ca/d2l/le/lessons/191328/topics/6491524
- `web.xml` — added configuration from Brightspace

**Template files:**
- `WEB-INF/faces-templates/layout.xhtml` — Used --DMIT Faces layout template--
- `webapp/resources/styles.css`

**Pages:**
- `index.xhtml`  — Used --DMIT Minimal Composition Page template--
- `aboutus.xhtml`  — Used --DMIT Minimal Composition Page template--

**Topics:** Application architecture, Strategy Pattern, CDI, service interfaces and implementations, in-memory services, Firebase introduction, Lombok, Jakarta Validation, Faces messages, OmniFaces Messages, and Facelets templates.

## Lesson 8: Development Templates and In-Memory CRUD

**Model:**
- `model/Student.java` — created from scratch; Student domain model

**Service:**
- `service/StudentService.java`
  - Template: **Model Service Interface**
  - Model class: `Student`
  - ID type: `String`
- `service/MemoryStudentService.java` — created from scratch; in-memory CRUD implementation

**View:**
- `view/StudentCrudView.java`
  - Template: **Faces CRUD Backing Bean**
  - Model class: `Student`
  - CDI: `memoryStudentService`
  - ID type: `String`

**CRUD Page:**
- `students/manage-students.xhtml`
  - Template: **CRUD Page**
  - Manage: `Students`
  - Model class: `Student`
  - ID type: `String`
  - Customized the generated `manage-students.xhtml`

**Topics:** IntelliJ file templates, Project Lombok, DataFaker, Jakarta Validation, JSF templates and composition, CRUD operations, managed beans, service interfaces, and in-memory service implementations.

## Lesson 9: Firebase Setup and CRUD Integration

In this lesson, we replace the in-memory Student service with Firebase Realtime Database while keeping the existing `StudentService` interface.



#### 1. Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Create a project**.
3. Use the naming convention:

   `dmit2015-1261-a02-YourGithubUsername`

4. Complete the project setup.



#### 2. Create a Realtime Database

1. In the Firebase project, go to **Build → Realtime Database**.
2. Click **Create Database**.
3. Select the database location.
4. Configure the database access/rules as demonstrated in class.
5. Open the **Data** tab.
6. Copy the **Realtime Database URL**.


This URL will be used to connect the application to Firebase.



#### 3. Test Firebase from IntelliJ

Right-click the **main project folder** and create a folder for the HTTP requests.

**Folder structure:**

```text
http_request/
└── firebase-rest-api.http
```

Create the HTTP request file using the IntelliJ template.

**Template:**
- **Firebase REST API HTTP Request**

**Template values:**
- Firebase URL: your Realtime Database URL
- JSON data path: `Student`
- Explicit value: `123`

Run the generated HTTP requests to test the connection between IntelliJ and Firebase Realtime Database.



#### 4. Create the Firebase Service

Inside the `service` package, create the Firebase service implementation.

**Template:**
- **DMIT2015 Model Service Interface Firebase HTTP Client Implementation**

**Template values:**
- Model class: `Student`

The Firebase implementation uses the existing `StudentService` interface.

This allows us to switch from:

```text
MemoryStudentService
```

to:

```text
FirebaseStudentService
```

while preserving the existing service interface.



#### 5. Configure MicroProfile Config

**MicroProfile Config** allows application settings to be stored separately from the Java source code and injected when the application runs.

Inside:

```text
src/main/resources/
```

create the `META-INF` folder and the following file:

```text
src/
└── main/
    └── resources/
        └── META-INF/
            └── microprofile-config.properties
```

Inside `microprofile-config.properties`, add:

```properties
firebase.rtdb.base.url=https://your-project-id-default-rtdb.firebaseio.com/
```

Replace the example URL with your actual Firebase Realtime Database URL.



#### 6. Update `StudentCrudView`

Open:

```text
view/StudentCrudView.java
```

Update the backing bean to use the Firebase service instead of the previous in-memory service.

Use the CDI name:

```java
@Named("currentStudentCrudView")
```

Replace the previous service injection/configuration with the Firebase service configuration demonstrated in class.




#### 7. Verify Firebase CRUD Operations

Run the application and test all CRUD operations:

- **Create** — add a Student
- **Read** — display Students
- **Update** — edit a Student
- **Delete** — remove a Student

Open:

**Firebase Console → Realtime Database → Data**

Verify that the changes made from the JSF application appear in Firebase.

Finally, restart the application and confirm that the Student data still exists.

Unlike `MemoryStudentService`, Firebase stores the data outside the running application, so the data remains after the application restarts.



**Topics:** Firebase Realtime Database, Firebase REST API, HTTP requests, CRUD operations, service interfaces, Firebase service implementation, MicroProfile Config, CDI, JSF backing beans, and persistent data.

## Lesson 10: Firebase Authentication 

In this lesson, we add **Firebase Authentication** to the existing Jakarta Faces application using email/password login.

Most authentication code is provided in the **Firebase Authentication Instructions on Brightspace**: https://lms.nait.ca/d2l/le/lessons/191328/topics/6251887

**Main focus:** Steps **3–7** and **9–11**.  
Steps **2 and 8** are discussed separately in lesson 11



#### 1. Enable Firebase Authentication — Step 1

In **Firebase Console**: https://console.firebase.google.com/u/0/?pli=1

1. Go to **Build → Authentication**.
2. Click **Get started**.
3. Open **Sign-in method**.
4. Enable **Email/Password**.
5. Go to **Authentication → Users**.
6. Add at least two test users.

Example:

```text
user01@dmit2015.ca
user02@dmit2015.ca
Password: Password2015
```

Each user receives a unique Firebase **UID**.



#### 2. Add Firebase Web API Key — Step 3

Register a **Web App** in Firebase and copy the **Web API Key**.

#### 1. Add the API Key to Ubuntu

In the Ubuntu Terminal, open `~/.profile`:

```bash
code ~/.profile
```

Add at the bottom:

```bash
export FIREBASE_WEB_API_KEY=YOUR_WEB_API_KEY
```

Replace `YOUR_WEB_API_KEY` with your Firebase Web API key.

#### 2. Load and Verify the Environment Variable

In the Ubuntu Terminal, run:

```bash
source ~/.profile
echo $FIREBASE_WEB_API_KEY
```

The second command should display your Firebase Web API key.

#### 3. Pass the Environment Variable to Tomcat

In IntelliJ IDEA, go to:

```text
Run → Edit Configurations → Tomcat
→ Startup/Connection → Run → Environment Variables
```

Add:

```text
FIREBASE_WEB_API_KEY = YOUR_WEB_API_KEY
```

Make sure **Pass environment variables** is checked.


#### 4. Restart Tomcat

Restart Tomcat so the new environment variable is available to the application.


---

#### 3. Authentication Model and Service — Step 4

Create:

```text
src/main/java/dmit2015/model/FirebaseAuthSignInResponsePayload.java
```

Copy code for `FirebaseAuthSignInResponsePayload.java` from **Step 4** of  https://lms.nait.ca/d2l/le/lessons/191328/topics/6251887.

It stores authentication information such as:



Create:

```text
src/main/java/dmit2015/service/FirebaseAuthService.java
```

Copy `FirebaseAuthService.java` from **Step 4**.

This service uses Java `HttpClient` to communicate with the **Firebase Authentication REST API**.



#### 4. Authentication Session — Step 5

Create:

```text
src/main/java/dmit2015/view/FirebaseAuthSignInSession.java
```

Copy the code from **Step 5**. https://lms.nait.ca/d2l/le/lessons/191328/topics/6251887

For our project, use:

The bean is session scoped:

```java
@Named("firebaseAuthSignInSession")
@SessionScoped
```

It stores the authentication information for the current logged-in user.

It reads:

```properties
firebase.web.api.key
```

from:

```text
microprofile-config.properties
```



#### 5. Login Backing Bean and Page — Step 6

Create the backing bean:

```text
src/main/java/dmit2015/view/FirebaseAuthSignIn.java
```

Copy `FirebaseAuthSignIn.java` from **Step 6**. https://lms.nait.ca/d2l/le/lessons/191328/topics/6251887



Then create the login page:

```text
src/main/webapp/firebaseAuthSignIn.xhtml
```

Create it as a **Facelets File** and copy the provided XHTML from **Step 6**.



#### 6. Add Logout — Step 7

Create:

```text
src/main/java/dmit2015/view/FacesLogout.java
```

Copy `FacesLogout.java` from **Step 7**. https://lms.nait.ca/d2l/le/lessons/191328/topics/6251887


Logout invalidates the current session using:

```java
Faces.invalidateSession();
```

The user is then redirected to the login page.





#### 7. Add Login and Logout to Layout — Step 10

Open the existing:

```text
src/main/webapp/WEB-INF/faces-templates/layout.xhtml
```

> Do not create another `layout.xhtml`.

Copy the Login/Logout menu code from **Step 10** into the existing navigation/menu.https://lms.nait.ca/d2l/le/lessons/191328/topics/6251887



The menu uses `firebaseAuthSignInSession` to determine whether to display **Login** or **Logout**.



#### 8. Protect the Student CRUD Page — Step 11 https://lms.nait.ca/d2l/le/lessons/191328/topics/6251887

Open:

```text
src/main/webapp/students/manage-students.xhtml
```

Inside the `maincontent` section, add:

```xml
<f:metadata>
    <f:viewAction action="#{firebaseAuthSignInSession.checkForToken}" />
</f:metadata>
```

Example:

```xml
<ui:define name="maincontent">

    <f:metadata>
        <f:viewAction action="#{firebaseAuthSignInSession.checkForToken}" />
    </f:metadata>

    <!-- Student CRUD content -->

</ui:define>
```

This checks whether the user is authenticated before allowing access to the Student CRUD page.



#### 9. Test Authentication

Run/redeploy the application and test:

- Open `students/manage-students.xhtml` without logging in
- Confirm you are redirected to `firebaseAuthSignIn.xhtml`
- Sign in with a Firebase Authentication user
- Confirm the Student CRUD page is accessible
- Confirm the logged-in user appears in the layout
- Click **Logout**
- Confirm the session is invalidated
- Try accessing the Student CRUD page again
- Confirm login is required again



**Main Files:**

```text
src/main/
├── java/dmit2015/
│   ├── model/
│   │   └── FirebaseAuthSignInResponsePayload.java
│   ├── service/
│   │   └── FirebaseAuthService.java
│   └── view/
│       ├── FirebaseAuthSignIn.java
│       ├── FirebaseAuthSignInSession.java
│       ├── FacesLogout.java
│       └── StudentCrudView.java
├── resources/
│   └── META-INF/
│       └── microprofile-config.properties
└── webapp/
    ├── firebaseAuthSignIn.xhtml
    ├── students/
    │   └── manage-students.xhtml
    └── WEB-INF/
        └── faces-templates/
            └── layout.xhtml
```

**Topics:** Firebase Authentication, Email/Password authentication, Firebase REST API, ID tokens, Firebase UID, session-scoped beans, MicroProfile Config, login/logout, protected JSF pages, CDI, and multi-tenant Firebase data.


## Lesson 11: Multi-Tenant Firebase Security

Lesson 10 focused on **authentication** — *Who is the user?*

Lesson 11 focuses on **authorization and multi-tenancy** — *Which data is the user allowed to access?*

Firebase Authentication gives us:

- `localId` — unique UID of the logged-in user
- `idToken` — proof that the user is authenticated

Student data is now stored under each user's UID so every authenticated user has their own data.


---

#### 1. Start the Existing Project

Run the Lesson 10 project first and verify:

- Login works
- Student CRUD page works after login
- Logout works

---

#### 2. Firebase Security Rules —  Step 2 from: https://lms.nait.ca/d2l/le/lessons/191328/topics/6251887

Go to:

```text
Firebase Console
→ Realtime Database
→ Rules
```

Add/update the security rules from **Step 2** of the Firebase Authentication instructions :https://lms.nait.ca/d2l/le/lessons/191328/topics/6251887

```text
multi_tenant_data
└── Student
    └── $uid
```

The important rule is:

```text
auth !== null && auth.uid === $uid
```

**What it does:** The user must be logged in and can access only data belonging to their own UID.

---

#### 3. Create the HTTP Request File

Create:

```text
http_request/
└── FirebaseAuthRTDB_HttpRequest.http
```

**Template:**

```text
DMIT2015 Firebase Auth + RTDB Http Client
```

**Template values:**

```text
Firebase API Key  → Firebase Web API key
Email             → Firebase test user email
Password          → Firebase test user password
Database URL      → Firebase Realtime Database URL
Domain Model Name → Student
ID Value          → new123
```

The sign-in request gets:

```text
localId
idToken
```

The remaining requests use:

```text
multi_tenant_data/Student/{{localId}}
```

and authenticate using:

```text
?auth={{idToken}}
```

**What it does:** Tests authenticated multi-tenant Firebase requests before changing the Java service.

---

#### 4. Test the HTTP Requests

Run **Sign in** first to get the `localId` and `idToken`.

Then test:

```text
PUT     → Create
GET     → Read
GET     → Read All
PUT     → Replace
PATCH   → Update part
DELETE  → Delete
```

If these requests work, Firebase Authentication, Security Rules, and authenticated Realtime Database access are working together.

---

#### 5. Create Multi-Tenant Service 

Create the service using the IntelliJ template:

```text
DMIT2015 Model Service Interface FirebaseRTDB Multi-Tenant Data Implementation
```

**Template value:**

```text
Model class: Student
```

Creates:

```text
src/main/java/dmit2015/service/
└── FirebaseMultiTenantHttpClientStudentService.java
```

The service is named:

```java
@Named("firebaseMultiTenantHttpClientStudentService")
```

It gets the logged-in user's:

```text
localId
idToken
```

from:

```text
FirebaseAuthSignInSession
```

**What it does:** Uses the logged-in user's UID for the Firebase data path and the ID token to authenticate CRUD requests.

---

#### 6. Switch the Service — Follow Step 9 form https://lms.nait.ca/d2l/le/lessons/191328/topics/6251887

Open:

```text
src/main/java/dmit2015/view/StudentCrudView.java
```

Previously:

```java
@Named("firebaseHttpClientStudentService")
```

Change the injected service to:

```java
@Inject
@Named("firebaseMultiTenantHttpClientStudentService")
private StudentService studentService;
```

**What it does:** Switches Student CRUD from the regular Firebase service to the multi-tenant Firebase service.

We do **not** change:

```text
StudentService.java
manage-students.xhtml
```

---

#### 7. Restart Tomcat

Stop Tomcat, rebuild/redeploy if needed, and start Tomcat again.

Then log in normally.

---

#### 8. Test User 1

Login with **Firebase User 1** and create a few students.

Firebase should store the data under User 1's UID:

```text
multi_tenant_data
└── Student
    └── USER_1_UID
        ├── student1
        └── student2
```

---

#### 9. Test User 2

Logout and login with **Firebase User 2**.

User 2 should **not see User 1's students**.

Create a student for User 2.

Firebase should now look like:

```text
multi_tenant_data
└── Student
    ├── USER_1_UID
    │   ├── student1
    │   └── student2
    │
    └── USER_2_UID
        └── student3
```

Each authenticated user now has their own Student data.

---

**Main Lesson 11 Files:**

```text
http_request/
└── FirebaseAuthRTDB_HttpRequest.http

src/main/java/dmit2015/
├── service/
│   └── FirebaseMultiTenantHttpClientStudentService.java
└── view/
    └── StudentCrudView.java
```

**Main idea:** Same application, same `StudentService` interface, and same CRUD page — but Firebase now separates and protects data by authenticated user.

# Week 5

Follow the code in the **`student-jpa-demo`** folder.



### Lesson 14: Jakarta Persistence (JPA) and H2 Database

In this lesson, we move from **in-memory/Firebase storage** to a **relational database using Jakarta Persistence (JPA)**.
---

#### 1. Create the Jakarta EE Project and Configure WildFly

Create a new project in IntelliJ IDEA:

```text
Project name:student-jpa-app
Project type: Jakarta EE
Build system: Maven
Jakarta EE: Web Profile
```

We use **WildFly** instead of Tomcat because the application now uses Jakarta EE services such as JPA, `EntityManager`, and transactions.

There are two ways to configure WildFly:

##### Option 1 — Install and Select WildFly in IntelliJ

For **Application Server**, select **WildFly**.

If WildFly is not already configured:

1. Click **New / Browse** beside Application Server.
2. Browse to your extracted WildFly folder.
3. Select the WildFly installation folder.
4. IntelliJ will recognize and add the server.

##### Option 2 — Use the WildFly Maven Plugin
> **Assignment 3 uses this approach:** configure WildFly through Maven and run the application using `mvn wildfly:dev`.

Instead of manually downloading and selecting WildFly in IntelliJ, configure the **WildFly Maven Plugin** in `pom.xml`.https://lms.nait.ca/d2l/le/lessons/191328/topics/6536257

The plugin provisions/downloads the required WildFly version:


Start WildFly and deploy the application from terminal using:

```bash
mvn wildfly:dev
```

**What it does:** Maven provisions the required WildFly server, starts it, and deploys the application.



---

#### 2. Project Configuration

Add the required Maven dependencies.

Create/update:

```text
pom.xml : https://lms.nait.ca/d2l/le/lessons/191328/topics/6528712 and plugins from step1 option 2 
web.xml: https://lms.nait.ca/d2l/le/lessons/191328/topics/6529717
```

These files configure the dependencies and Jakarta EE application.

---

#### 3. Configure the H2 Database

Create:

```text
src/main/java/dmit2015/config/ApplicationConfig.java
```

**Template:**

```text
DMIT2015 Jakarta Persistence ApplicationConfig
```

Use the H2 file-based database:

```text
jdbc:h2:file:./data/Lesson12DemoDB;
```

The DataSource is:

```text
H2DatabaseDS
```

**What it does:** Configures the H2 database connection used by the application.

---

#### 4. Configure JPA

Create:

```text
src/main/resources/META-INF/persistence.xml
```

**Template:**

```text
DMIT2015 Jakarta Persistence persistence.xml
```

It uses the `H2DatabaseDS` DataSource created in `ApplicationConfig.java`.

JPA uses **Hibernate** as the persistence provider.

Important development setting:

```text
drop-and-create
```

This drops and recreates the database tables when the application starts, so existing data can be lost.

When the entity structure is stable, it can be changed to:

```text
none
```

to preserve existing data.

**What it does:** Configures JPA/Hibernate and tells it which database connection to use.

---

#### 5. Create the Student JPA Entity

Create:

```text
src/main/java/dmit2015/model/Student.java
```

**Template:**

```text
DMIT2015 Jakarta Persistence Entity Class
```

**Template values:**

```text
EntityName:          Student
PrimaryKeyDataType:  Long
CsvColumnLength:     leave default
```

Important annotations:

```text
@Entity          → Maps Student to a database table
@Id              → Primary key
@GeneratedValue  → Automatically generates the ID
@Version         → Tracks record versions
@NotBlank        → Validates required fields
@PrePersist      → Sets timestamps when creating
@PreUpdate       → Updates timestamp when updating
```

The `of()` method uses `Faker` to generate sample Student data.

**What it does:** Turns the Java `Student` class into a JPA entity that can be stored in the H2 database.

---

#### 6. Create the Student Service Interface

Create:

```text
src/main/java/dmit2015/service/StudentService.java
```

**Template:**

```text
DMIT2015 Model Service template
```

The service interface defines CRUD operations such as:

```text
Create
Find
Find All
Update
Delete
```

**What it does:** Defines the operations available for Student data without containing database code.

---

#### 7. Create the JPA Service

Create:

```text
src/main/java/dmit2015/service/StudentJpaService.java
```

**Template:**

```text
DMIT2015 Model Service Interface Jakarta Persistence Implementation
```

**Template values:**

```text
ModelClass:          Student
PrimaryKeyDataType:  Long
primaryKey:          id
```

The service uses:

```java
@PersistenceContext
private EntityManager entityManager;
```

`EntityManager` performs the database operations:

```text
persist() → Create
find()    → Read
JPQL      → Read All
merge()   → Update
remove()  → Delete
```

Update `updateStudent()` with the Student properties:

```java
existingStudent.setFirstName(student.getFirstName());
existingStudent.setLastName(student.getLastName());
existingStudent.setCourseSection(student.getCourseSection());

student = entityManager.merge(existingStudent);
```

`@Transactional` is used for operations that change database data.

**What it does:** Implements `StudentService` CRUD operations using JPA and `EntityManager`.

---

#### 8. Create the Student CRUD Backing Bean

Create the `view` package:

```text
src/main/java/dmit2015/view/
```

Create:

```text
StudentCrudView.java
```

**Template:**

```text
DMIT2015 Faces CRUD Backing Bean
```

**Template values:**

```text
ModelClass:               Student
cdiServiceInstanceName:   studentService
PrimaryKeyDataType:       Long
```

Update the injected service:

```java
@Inject
@Named("jakartaPersistenceStudentService")
private StudentService studentService;
```

**What it does:** Connects the JSF page to `StudentService`. The backing bean does not access `EntityManager` or H2 directly.

---

#### 9. Create the Faces Layout

Create:

```text
src/main/webapp/WEB-INF/faces-templates/layout.xhtml
```

**Template:**

```text
DMIT2015 Faces Template
```

**What it does:** Provides the shared layout for the Faces pages, including navigation, messages, styles, footer, and page content.

---

#### 10. Create the Student CRUD Page

Create:

```text
src/main/webapp/students/manage-students.xhtml
```

**Template:**

```text
DMIT2015 Faces CRUD Page
```

**Template values:**

```text
File name:          manage-students
ModelClass:         Student
PrimaryKeyField:    id
```

Replace the generated DataTable placeholder fields with:

```text
firstName
lastName
courseSection
```

Also replace the Create/Edit dialog placeholder fields with:

```text
firstName
lastName
courseSection
```

**What it does:** Displays and manages Student records stored in the H2 database.

---

#### 11. Create the Home Page

Create:

```text
src/main/webapp/index.xhtml
```

**Template:**

```text
DMIT2015 Faces Composition Page
```

**Template values:**

```text
File name:   index
PAGE TITLE:  Home
```

**What it does:** Creates the application's home page using the shared Faces layout.

---

#### 12. Create Sample Student Data

Create the initializer using:

```text
DMIT2015 Jakarta Persistence Entity Class Initializer
```

Import the Student model:

```java
import dmit2015.model.Student;
```

Change:

```java
if (studentJpaService.count() == 0) {
```

to:

```java
if (studentJpaService.getAllStudents().isEmpty()) {
```

Inside the `try` block, generate 22 sample students:

```java
var faker = new Faker();

for (int count = 1; count <= 22; count++) {
    Student currentStudent = Student.of(faker);
    studentJpaService.createStudent(currentStudent);
}
```

Change:

```java
logger.info("Created " + studentJpaService.count() + " records.");
```

to:

```java
logger.info("Created " + studentJpaService.getAllStudents().size() + " records.");
```

**What it does:** Uses DataFaker to automatically create sample Student records when the database is empty.

---

#### 13. View the H2 Database

Run the application and open the H2 Console:

```text
http://localhost:8080/student-jpa-app-1.0-SNAPSHOT/h2-console/
```

Use the H2 Console to view the database tables and Student records created by JPA.

---

