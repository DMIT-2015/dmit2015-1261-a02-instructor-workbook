# DMIT2015 Fall 2026 Term Workbook Repository

This repository contains course work that is **not submitted for marks**, including:

- Examples completed in class
- Code from slides
- Exercises and practice activities

Clone this repository to keep track of your work and easily share code with the instructor.

## Week 1

Follow the code in the **`dmit2015-javase-demo`** folder.

## Week 2

Follow the code in the **`dmit2015-faces-demo`** folder.

### Lesson 4: Introduction to Jakarta Faces

**Coding files:**
- `HelloBean.java`
- `GreetingBean.java`
- `hello.xhtml`
- `index.xhtml`

**Topics:** Jakarta Faces, Maven WAR projects, Tomcat 11, Facelets, CDI managed beans, EL binding, and PrimeFaces.

### Lesson 5: JSF Components, Form Binding & Validation

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

## Week 3–4

Follow the code in the **`dmit2015-faces-firebase-demo`** folder.

### Lesson 7: Architecture and Strategy Pattern

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

### Lesson 8: Development Templates and In-Memory CRUD

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

### Lesson 9: Firebase Setup and CRUD Integration

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
