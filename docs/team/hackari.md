---
layout: page
title: Ryan Sim's Project Portfolio Page
---

### Project: NUSConnect

NUSConnect - It helps NUS students efficiently organise and manage the contact details of peers they meet during
tutorials and group projects. It is designed for users who prefer a CLI. It allows for custom labeling, reminders, easy
updates, and is optimised for building and maintaining academic connections throughout their university journey.
The user interacts with it using a CLI, and it has a GUI created with JavaFX.
It is written in Java.

Given below are my contributions to the project.

* **UI color design**: 
  * Overhauled theme from base project, AB3, to better fit NUSConnect's aesthetic with the National University of Singapore, with the help of mockup from group member Lee Jia Wei.
* **Project management**:
  * Compiled tasks that needs to be done for each milestone for everyone to choose from.
* **Enhancements to existing features**:
  * `Delete`: Added functionality to delete multiple people at once.
  * Regular Expression Matching: Wrote code and tests to test validity of newly added fields apart from Modules.
  * Data Structure: Changed all fields apart from Name and Telegram to be Optional fields.
  * Storage: Changed the JSON loader to be able to parse null values that were possible after the above change.
* **Documentation**:
  * User Guide:
    * Updated the section `help` to reflect the new UI.
  
  *  Developer Guide:
      * Added initial table of User Stories, and updated it in v1.5. 
      * Updated UML sequence diagram for Delete to reflect deleting multiple persons.

