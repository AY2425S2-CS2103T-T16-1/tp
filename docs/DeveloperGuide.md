---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* {list here sources of all reused/adapted ideas, code, documentation, and third-party libraries -- include links to the original source as well}

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams in this document `docs/diagrams` folder. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

Given below is a quick overview of main components and how they interact with each other.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* implements its functionality using a concrete `{Component Name}Manager` class (which follows the corresponding API `interface` mentioned in the previous point.

For example, the `Logic` component defines its API in the `Logic.java` interface and implements its functionality using the `LogicManager.java` class which follows the `Logic` interface. Other components interact with a given component through its interface rather than the concrete class (reason: to prevent outside component's being coupled to the implementation of a component), as illustrated in the (partial) class diagram below.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` that is made up of parts e.g.`CommandBox`, `ResultDisplay`, `PersonListPanel`, `StatusBarFooter` etc. All these, including the `MainWindow`, inherit from the abstract `UiPart` class which captures the commonalities between classes that represent parts of the visible GUI.

The `UI` component uses the JavaFx UI framework. The layout of these UI parts are defined in matching `.fxml` files that are in the `src/main/resources/view` folder. For example, the layout of the [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java) is specified in [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml)

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component, as it displays `Person` object residing in the `Model`.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1 2")` API call as an example.

![Interactions Inside the Logic Component for the `delete 1 2` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X) but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, it is passed to an `AddressBookParser` object which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above (for simplicity), in the code it can take several interactions (between the command object and the `Model`) to achieve.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned back from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name e.g., `AddCommandParser`) which uses the other classes shown above to parse the user command and create a `XYZCommand` object (e.g., `AddCommand`) which the `AddressBookParser` returns back as a `Command` object.
* All `XYZCommandParser` classes (e.g., `AddCommandParser`, `DeleteCommandParser`, ...) inherit from the `Parser` interface so that they can be treated similarly where possible e.g, during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object) and all `Group` objects (which are contained in a `UniqueGroupList` object).
* stores the currently 'selected' `Person` objects (e.g., results of a search query) as a separate _filtered_ list which is exposed to outsiders as an unmodifiable `ObservableList<Person>` that can be 'observed' e.g. the UI can be bound to this list so that the UI automatically updates when the data in the list change.
* stores a `UserPref` object that represents the user’s preferences. This is exposed to the outside as a `ReadOnlyUserPref` objects.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** An alternative (arguably, a more OOP) model is given below. It has a `Module` list in the `AddressBook`, which `Person` references. This allows `AddressBook` to only require one `Module` object per unique module, instead of each `Person` needing their own `Module` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* inherits from both `AddressBookStorage` and `UserPrefStorage`, which means it can be treated as either one (if only the functionality of only one is needed).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Import feature
#### Implementation
The Import feature allows users to load an existing address book from a JSON file into the application. This feature provides a seamless way to integrate external data and ensures that users can transfer their address book information efficiently without manual entry.

This high-level sequence diagram outlines the flow of interactions between the components involved in the import functionality. Here's an elaboration on each part of the diagram:

<img src="images/ImportSequenceDiagram.png" width="450" />

1. User Input:
   User initiates the import process by entering the command import filename via the UI. This command indicates the user's intention to import an address book from a file located at the specified filename path.


2. UI - Parsing the Command:
   The UI receives the user's input and sends the command (import filename) to the Parser.


3. Parser - Passing Command to Logic:
   After parsing the input, the Parser creates the appropriate Logic command and forwards the parsed information (like the file path) to Logic for execution. The Logic component is the core handler that manages the overall operation of importing the data and updating the application state.


4. Logic - Invoking Storage:
   Logic interacts with Storage to begin the import process. Specifically, it calls importAddressBook(filePath), passing the file path of the address book file that needs to be imported.


5. Storage - Updating AddressBook:
   The Storage component reads the JSON (or other file format) data from the specified file and deserializes it into the AddressBook model.
   Once the data is read successfully, Storage updates the internal AddressBook with the newly imported data, effectively replacing or merging the previous address book data with the imported data.


6. Storage - Returning Control to Logic:
   After the AddressBook has been updated, Storage sends a response back to Logic, confirming that the import was successful and the address book has been updated.


7. Logic - Returning Status to UI:
   Logic then communicates with the UI to update it on the status of the import operation. This can be either a success message or an error message, depending on whether the operation was successful or if there were issues (e.g., file format errors).


8. UI - Displaying Result to User:
   Finally, the UI displays the result to the User, letting them know whether the import was successful or if there were any errors encountered during the process. This step ensures that the user is kept informed about the status of the operation.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, configuration, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [Configuration guide](Configuration.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:
NUS students

* has a need to manage a significant number of contacts (e.g. classmates from different modules, CCA mates, etc)
* prefer desktop apps over other types
* can type fast
* prefers typing to mouse interactions
* is reasonably comfortable using CLI apps

**Value proposition**: A fast and efficient contact management tool for NUS students.
NUSConnect helps students quickly add, organise and find contacts with minimal effort.


### User Stories

<table>
    <thead>
        <tr>
            <th>Priority</th>
            <th>As a …</th>
            <th>I want to …</th>
            <th>So that I can …</th>
        </tr>
    </thead>
    <tbody>
        <tr class="high-priority">
            <td>***</td>
            <td>Student</td>
            <td>Link multiple contact methods to a single person (e.g., email, phone)</td>
            <td>Communicate through multiple channels as needed</td>
        </tr>
        <tr class="high-priority">
            <td>***</td>
            <td>Student</td>
            <td>Search for a contact by name</td>
            <td>Retrieve contact details quickly</td>
        </tr>
        <tr class="high-priority">
            <td>***</td>
            <td>Student</td>
            <td>Search for a contact by module</td>
            <td>Retrieve contact details quickly</td>
        </tr>
        <tr class="high-priority">
            <td>***</td>
            <td>Student</td>
            <td>Delete a contact from the address book</td>
            <td>Remove outdated or unnecessary contacts</td>
        </tr>
        <tr class="high-priority">
            <td>***</td>
            <td>Student</td>
            <td>Edit a contact’s details</td>
            <td>Ensure information remains accurate and up to date</td>
        </tr>
        <tr class="high-priority">
            <td>***</td>
            <td>Student</td>
            <td>View a contact’s profile with their full details</td>
            <td>Access comprehensive details when needed</td>
        </tr>
        <tr class="medium-priority">
            <td>**</td>
            <td>Student</td>
            <td>Delete all contacts in bulk if needed</td>
            <td>Reset my contact list when necessary</td>
        </tr>
        <tr class="medium-priority">
            <td>**</td>
            <td>Student</td>
            <td>Delete multiple contacts from the address book</td>
            <td>Remove outdated or unnecessary contacts efficiently</td>
        </tr>
        <tr class="medium-priority">
            <td>**</td>
            <td>Student</td>
            <td>Add personal notes to a contact’s profile</td>
            <td>Preserve important contextual information about a contact</td>
        </tr>
        <tr class="medium-priority">
            <td>**</td>
            <td>Project group leader</td>
            <td>Create a contact group specific to a project</td>
            <td>Easily access project members’ details for coordination</td>
        </tr>
        <tr class="low-priority">
            <td>*</td>
            <td>Student</td>
            <td>Export my contact list</td>
            <td>Share it with others or to save it</td>
        </tr>
        <tr class="low-priority">
            <td>*</td>
            <td>Student</td>
            <td>Import a contact list</td>
            <td>Load previously saved or other's versions of the contact list</td>
        </tr>
    </tbody>
</table>

### Use cases

(For all use cases below, the **System** is the `NUSConnect` and the **Actor** is the `user`, unless specified otherwise)

**Use case: Delete a contact**

**MSS**

1.  User requests to list contacts
2.  NUSConnect shows a list of contacts
3.  User requests to delete a specific contact in the list
4.  NUSConnect deletes the contact

    Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. NUSConnect shows an error message.

      Use case resumes at step 2.

**Use case: Delete multiple contacts**

**MSS**

1.  User requests to list contacts
2.  NUSConnect shows a list of contacts
3.  User requests to delete a list of contacts in the list
4.  NUSConnect deletes the contacts

    Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. At least one of the indices is invalid.

    * 3a1. NUSConnect shows an error message.

      Use case resumes at step 2.


**Use case: Adding a person**


**MSS**

1.  User request to add a contact
2.  NUSConnect adds the contact

    Use case ends.


**Use case: Listing directory**

**MSS**

1.  User request to list contacts
2.  NUSConnect shows the list of contacts

    Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.


**Use case: Creating a group**

**MSS**

1. User requests to create a group
2. NUSConnect creates the group

    Use case ends.


**Use case: Adding a contact to a group**

**MSS**

1. User requests to list contacts
2. NUSConnect shows a list of contacts
3. User requests to add a contact to a group
4. NUSConnect adds the contact to the chosen group

    Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. Either the person index or group index is invalid.

    * 3a1. NUSConnect shows an error message.

      Use case resumes at step 2.


**Use case: Deleting a group**

**MSS**

1. NUSConnect shows a list of groups
2. User requests to delete a specific group in the group list
3. NUSConnect deletes the group

    Use case ends.

**Extensions**

* 1a. The group list is empty.

  Use case ends.

* 2a. The given group index is invalid.

    * 3a1. NUSConnect shows an error message.

      Use case resumes at step 1.

*{More to be added}*

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `17` or above installed.
2.  Should be able to hold up to 1000 persons without a noticeable sluggishness in performance for typical usage.
3.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
4.  Should not require additional installation or system modifications beyond Java.
5.  There should not be loss of data while using the application.

### Glossary

* **Mainstream OS**: Windows, Linux, Unix, MacOS
* **Private contact detail**: A contact detail that is not meant to be shared with others.
* **Major**: A primary major of a student, such as Computer Science.
* **Module**: An NUS Module, such as CS2103T.
* **Group**: A group of people in the address book.
--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. Initial launch

   1. Download the jar file and copy into an empty folder

   1. Double-click the jar file Expected: Shows the GUI with a set of sample contacts. The window size may not be optimum.

1. Saving window preferences

   1. Resize the window to an optimum size. Move the window to a different location. Close the window.

   1. Re-launch the app by double-clicking the jar file.<br>
       Expected: The most recent window size and location is retained.

1. _{ more test cases …​ }_

### Deleting people

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command. Multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: First contact is deleted from the list. Details of the deleted contact shown in the status message. Timestamp in the status bar is updated.

   1. Test case: `delete 1 2 1 2`<br>
      Expected: The first and second contacts are deleted from the list. Details of the deleted contact shown in the status message. Timestamp in the status bar is updated.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. Error details shown in the status message. Status bar remains the same.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases …​ }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{explain how to simulate a missing/corrupted file, and the expected behavior}_

1. _{ more test cases …​ }_

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Planned Enhancements**

Team size: 5

1. **Make group delete** more versatile. The current group delete command is only able to delete the entire group. This
is too restrictive. We plan to make `group delete` be able to delete specific member from a group. Adjust `group delete`
to accept this format `group delete PERSON_INDEX from GROUP_INDEX`  so that we can remove specific member.


2. Currently, every person's details like `telegram`, `email` and others are stored in the group array in `addressbook.json`. Since only `name` is typically used within
group context, we plan to update the group structure in storage to store only the person name. This keeps the JSON smaller.
