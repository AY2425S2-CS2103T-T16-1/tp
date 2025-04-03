package nusconnect.logic.commands;

import static java.util.Objects.requireNonNull;
import static nusconnect.logic.parser.CliSyntax.PREFIX_ALIAS;
import static nusconnect.logic.parser.CliSyntax.PREFIX_COURSE;
import static nusconnect.logic.parser.CliSyntax.PREFIX_EMAIL;
import static nusconnect.logic.parser.CliSyntax.PREFIX_MODULE;
import static nusconnect.logic.parser.CliSyntax.PREFIX_NAME;
import static nusconnect.logic.parser.CliSyntax.PREFIX_NOTE;
import static nusconnect.logic.parser.CliSyntax.PREFIX_PHONE;
import static nusconnect.logic.parser.CliSyntax.PREFIX_TELEGRAM;
import static nusconnect.logic.parser.CliSyntax.PREFIX_WEBSITE;
import static nusconnect.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import nusconnect.commons.core.index.Index;
import nusconnect.commons.util.ToStringBuilder;
import nusconnect.logic.Messages;
import nusconnect.logic.commands.exceptions.CommandException;
import nusconnect.model.Model;
import nusconnect.model.module.Module;
import nusconnect.model.person.Alias;
import nusconnect.model.person.Course;
import nusconnect.model.person.Email;
import nusconnect.model.person.Name;
import nusconnect.model.person.Note;
import nusconnect.model.person.Person;
import nusconnect.model.person.Phone;
import nusconnect.model.person.Telegram;
import nusconnect.model.person.Website;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by the index number used in the displayed person list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_TELEGRAM + "TELEGRAM] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ALIAS + "ALIAS] "
            + "[" + PREFIX_COURSE + "COURSE] "
            + "[" + PREFIX_NOTE + "NOTE] "
            + "[" + PREFIX_WEBSITE + "WEBSITE] "
            + "[" + PREFIX_MODULE + "MODULE]...\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_EMAIL + "johndoe@example.com";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited Person: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in the address book.";

    private final Index index;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        if (!personToEdit.isSamePerson(editedPerson) && model.hasPerson(editedPerson)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson)), index);
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        assert personToEdit != null;

        Name updatedName = editPersonDescriptor.getIsNameEdited() ? editPersonDescriptor.getName()
                : personToEdit.getName();
        Telegram updatedTelegram = editPersonDescriptor.getIsTelegramEdited() ? editPersonDescriptor.getTelegram()
                : personToEdit.getTelegram();
        Phone updatedPhone = editPersonDescriptor.getIsPhoneEdited() ? editPersonDescriptor.getPhone()
                : personToEdit.getPhone().orElse(null);
        Email updatedEmail = editPersonDescriptor.getIsEmailEdited() ? editPersonDescriptor.getEmail()
                : personToEdit.getEmail().orElse(null);
        Alias updatedAlias = editPersonDescriptor.getIsAliasEdited() ? editPersonDescriptor.getAlias()
                : personToEdit.getAlias().orElse(null);
        Course updatedCourse = editPersonDescriptor.getIsCourseEdited() ? editPersonDescriptor.getCourse()
                : personToEdit.getCourse().orElse(null);
        Note updatedNote = editPersonDescriptor.getIsNoteEdited() ? editPersonDescriptor.getNote()
                : personToEdit.getNote().orElse(null);
        Website updatedWebsite = editPersonDescriptor.getIsWebsiteEdited() ? editPersonDescriptor.getWebsite()
                : personToEdit.getWebsite().orElse(null);
        Set<Module> updatedModules = editPersonDescriptor.getTags().orElse(personToEdit.getModules());

        return new Person(updatedName, updatedTelegram, updatedPhone,
                updatedEmail, updatedAlias, updatedCourse, updatedNote, updatedWebsite, updatedModules);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand)) {
            return false;
        }

        EditCommand otherEditCommand = (EditCommand) other;
        return index.equals(otherEditCommand.index)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. The corresponding booleans indicate if each field should be replaced.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Telegram telegram;
        private Phone phone;
        private Email email;
        private Alias alias;
        private Course course;
        private Note note;
        private Website website;
        private Set<Module> modules;
        private boolean isNameEdited = false;
        private boolean isTelegramEdited = false;
        private boolean isPhoneEdited = false;
        private boolean isEmailEdited = false;
        private boolean isAliasEdited = false;
        private boolean isCourseEdited = false;
        private boolean isNoteEdited = false;
        private boolean isWebsiteEdited = false;
        private boolean isModulesEdited = false;
        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code modules} is used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setTelegram(toCopy.telegram);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setAlias(toCopy.alias);
            setCourse(toCopy.course);
            setNote(toCopy.note);
            setWebsite(toCopy.website);
            setModules(toCopy.modules);
            setIsNameEdited(toCopy.isNameEdited);
            setIsTelegramEdited(toCopy.isTelegramEdited);
            setIsPhoneEdited(toCopy.isPhoneEdited);
            setIsEmailEdited(toCopy.isEmailEdited);
            setIsAliasEdited(toCopy.isAliasEdited);
            setIsCourseEdited(toCopy.isCourseEdited);
            setIsNoteEdited(toCopy.isNoteEdited);
            setIsWebsiteEdited(toCopy.isWebsiteEdited);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return isNameEdited || isTelegramEdited || isPhoneEdited || isEmailEdited
                    || isAliasEdited || isCourseEdited || isNoteEdited || isWebsiteEdited || isModulesEdited;
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Name getName() {
            return name;
        }

        public void setIsNameEdited(boolean isNameEdited) {
            this.isNameEdited = isNameEdited;
        }

        public Boolean getIsNameEdited() {
            return isNameEdited;
        }

        public void setTelegram(Telegram telegram) {
            this.telegram = telegram;
        }

        public Telegram getTelegram() {
            return telegram;
        }

        public void setIsTelegramEdited(boolean isTelegramEdited) {
            this.isTelegramEdited = isTelegramEdited;
        }

        public Boolean getIsTelegramEdited() {
            return isTelegramEdited;
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Phone getPhone() {
            return phone;
        }

        public void setIsPhoneEdited(boolean isPhoneEdited) {
            this.isPhoneEdited = isPhoneEdited;
        }

        public Boolean getIsPhoneEdited() {
            return isPhoneEdited;
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Email getEmail() {
            return email;
        }

        public void setIsEmailEdited(boolean isEmailEdited) {
            this.isEmailEdited = isEmailEdited;
        }

        public Boolean getIsEmailEdited() {
            return isEmailEdited;
        }

        public void setAlias(Alias alias) {
            this.alias = alias;
        }

        public Alias getAlias() {
            return alias;
        }

        public void setIsAliasEdited(boolean isAliasEdited) {
            this.isAliasEdited = isAliasEdited;
        }

        public Boolean getIsAliasEdited() {
            return isAliasEdited;
        }

        public void setCourse(Course course) {
            this.course = course;
        }

        public Course getCourse() {
            return course;
        }

        public void setIsCourseEdited(boolean isCourseEdited) {
            this.isCourseEdited = isCourseEdited;
        }

        public Boolean getIsCourseEdited() {
            return isCourseEdited;
        }

        public void setNote(Note note) {
            this.note = note;
        }

        public Note getNote() {
            return note;
        }

        public void setIsNoteEdited(boolean isNoteEdited) {
            this.isNoteEdited = isNoteEdited;
        }

        public Boolean getIsNoteEdited() {
            return isNoteEdited;
        }

        public void setWebsite(Website website) {
            this.website = website;
        }

        public Website getWebsite() {
            return website;
        }

        public void setIsWebsiteEdited(boolean isWebsiteEdited) {
            this.isWebsiteEdited = isWebsiteEdited;
        }

        public Boolean getIsWebsiteEdited() {
            return isWebsiteEdited;
        }

        /**
         * Sets {@code modules} to this object's {@code modules}.
         * A defensive copy of {@code modules} is used internally.
         */
        public void setModules(Set<Module> modules) {
            this.modules = (modules != null) ? new HashSet<>(modules) : null;
            this.isModulesEdited = true;
        }

        /**
         * Returns an unmodifiable module set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code modules} is null.
         */
        public Optional<Set<Module>> getTags() {
            return (modules != null) ? Optional.of(Collections.unmodifiableSet(modules)) : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor)) {
                return false;
            }

            EditPersonDescriptor otherEditPersonDescriptor = (EditPersonDescriptor) other;
            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(telegram, otherEditPersonDescriptor.telegram)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(email, otherEditPersonDescriptor.email)
                    && Objects.equals(alias, otherEditPersonDescriptor.alias)
                    && Objects.equals(course, otherEditPersonDescriptor.course)
                    && Objects.equals(note, otherEditPersonDescriptor.note)
                    && Objects.equals(website, otherEditPersonDescriptor.website)
                    && Objects.equals(modules, otherEditPersonDescriptor.modules);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("telegram", telegram)
                    .add("phone", phone)
                    .add("email", email)
                    .add("alias", alias)
                    .add("course", course)
                    .add("note", note)
                    .add("website", website)
                    .add("modules", modules)
                    .toString();
        }
    }
}
