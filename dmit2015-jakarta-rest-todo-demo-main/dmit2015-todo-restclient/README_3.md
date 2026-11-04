# DMIT2015 — REST Client

README 3 of 3: PrimeFaces CRUD Page and Testing


## Introduction — What are we building in this part?

In README 1, we created the MicroProfile REST Client. In README 2, we connected it to a service and a Jakarta Faces backing bean.

Now we'll build the web page that lets users manage to-do items.

The finished application will let users view, create, edit, and delete tasks using PrimeFaces components. Those actions will go through our backing bean and service to the existing REST API.

We'll also set up the shared page layout, add navigation, and test that the client and API use the same data.

## Part 1 — Create the CRUD page

### Why do we need it?

Our backing bean already contains the logic for handling to-do items, but users need a page with a table, buttons, and input fields.

We'll use the DMIT2015 Faces CRUD Page template instead of writing the entire page manually.

### Step 1 — Generate crud.xhtml

In IntelliJ IDEA, open the REST Client module.

Under `src/main/webapp`, create these directories:

```text
restapi/
└── todo/
```

Inside `todo`, create a new file using the DMIT2015 Faces CRUD Page template.

Enter:

| Template setting     | Value        |
| -------------------- | ------------ |
| File name            | `crud.xhtml` |
| Model class          | `TodoItem`   |
| Primary key property | `id`         |

File: `src/main/webapp/restapi/todo/crud.xhtml`

The template generates the table, New button, edit and delete buttons, dialogs, and Save/Cancel actions.

Why use the template? It provides the standard PrimeFaces CRUD structure. We only need to customize the fields for our `TodoItem` model.

### Step 2 — Fix the layout file location

We will encounters a missing layout-template path after generating the page.

In the starter project, the layout is located at:

`src/main/webapp/WEB-INF/layout.xhtml`

But the generated CRUD page expects:

`/WEB-INF/faces-templates/layout.xhtml`

In IntelliJ:

1. Under `WEB-INF`, create a directory named `faces-templates`.
2. Move the existing `layout.xhtml` into that directory.

The resulting location should be:

`src/main/webapp/WEB-INF/faces-templates/layout.xhtml`

At the beginning of `crud.xhtml`, the composition uses:

```text
template="/WEB-INF/faces-templates/layout.xhtml"
```

Why: The layout provides the shared navigation, styles, messages, and page structure. The CRUD page must reference its actual location.

We are moving the existing layout, not creating a new one from scratch.

## Part 2 — Customize the TodoItem table

### Why do we need it?

The generated page contains placeholder columns. We need to replace them with the actual properties of `TodoItem`.

Things to display:

- ID
- Name
- Complete
- Version

### Step 3 — Configure the table columns


Inside the table, use these columns:

```xml
<p:column headerText="ID" filterBy="#{currentItem.id}">
    <h:outputText value="#{currentItem.id}"/>
</p:column>

<p:column headerText="Name" filterBy="#{currentItem.name}">
    <h:outputText value="#{currentItem.name}"/>
</p:column>

<p:column headerText="Complete">
    <p:selectBooleanCheckbox value="#{currentItem.complete}"
                             disabled="true"
                             readonly="true"/>
</p:column>

<p:column headerText="Version">
    <h:outputText value="#{currentItem.version}"/>
</p:column>
```

### Understand the code

| Component                 | Purpose                                                         |
| ------------------------- | --------------------------------------------------------------- |
| `h:outputText`            | Displays text such as ID, name, or version                      |
| `p:selectBooleanCheckbox` | Displays the true/false completion status as a checkbox         |
| `disabled="true"`         | Prevents users from changing the checkbox directly in the table |
| `version`                 | Lets us observe version changes when records are updated        |


## Part 3 — Customize the create/edit dialog

### Why do we need it?

The table displays existing records, but users also need a form to enter or change a task.

Our `TodoItem` model has four properties, but the user only needs to edit:

- `name`
- `complete`

The ID is assigned by the API, and the version is managed during updates.

### Step 4 — Add the Name field

Inside the generated dialog's `manage-TodoItem-content` panel, use:

```xml
<div class="field">
    <p:outputLabel for="@next">Name</p:outputLabel>
    <p:inputText
            id="name"
            value="#{currentTodoItemCrudView.selectedTodoItem.name}">
    </p:inputText>
</div>
```

Why: `p:inputText` allows the user to enter a task name.

The expression `#{currentTodoItemCrudView.selectedTodoItem.name}` connects the input to the selected `TodoItem` in the backing bean.

### Step 5 — Add the Complete checkbox

Below the Name field, add:

```xml
<div class="field">
    <p:outputLabel for="@next">Complete</p:outputLabel>
    <p:selectBooleanCheckbox
            id="complete"
            value="#{currentTodoItemCrudView.selectedTodoItem.complete}">
    </p:selectBooleanCheckbox>
</div>
```

Why: `complete` is a boolean, so a checkbox is more appropriate than a text input.

Unlike the checkbox in the table, this checkbox is editable because it is part of the create/edit form.


## Part 4 — Create the home page and navigation

### Why do we need it?

The starter project doesn't contain the Faces home page. We need a starting page and a menu link so users can reach the CRUD page.

### Step 7 — Generate index.xhtml

Under:

`src/main/webapp`

Create `index.xhtml` using the  DMIT2015 Faces Minimum Composition Page template.


Why: This page uses the shared layout and provides a home screen for the REST Client application.

### Step 8 — Add the Todo menu link

Open:

`src/main/webapp/WEB-INF/faces-templates/layout.xhtml`

Find the `<p:menubar>` inside the navigation section.

Add the Todo menu item after Home:

```xml
    <p:menuitem value="Todo"
                outcome="/restapi/todo/crud.xhtml"/>
```

Why: The `outcome` tells Jakarta Faces which page to open when the menu item is selected.


## Part 5 — Run and test the REST Client

### Why do we need to test both applications?

Our REST Client and REST API are separate applications. We need to confirm that the web page sends requests to the API and that changes made through the page are reflected in the API's data.

### Step 9 — Start both applications

In IntelliJ, use the existing run configurations:

1. Start the REST API on port `8090`.
2. Start the REST Client on port `8080.
3. Open the REST Client application in your browser.
4. Select Todo from the navigation menu.

The exact browser URL is localhost:8080

### Step 10 — Test creating an item

On the Todo page:

1. Click New.
2. Enter a task name.
3. Optionally select Complete.
4. Click Save.

The new item should appear in the table.

What happens behind the scenes?

`crud.xhtml` → `TodoItemCrudView.onSave()` → `TodoItemService.createTodoItem()` → `TodoItemMpRestClient.create()` → REST API

The REST API creates the record, and the client displays it.

### Step 11 — Test editing and version changes

1. Click the pencil icon beside an existing item.
2. Change its name or completion status.
3. Click Save.
4. Check the Version column.


## What did we accomplish?

We generated the PrimeFaces CRUD page, configured its fields and table columns, corrected the layout location, added the home page and navigation, and tested the application against the existing REST API.

This completes the three-part REST Client.
