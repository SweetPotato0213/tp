package jeryl.fyp.logic.commands;

import static jeryl.fyp.logic.commands.CommandTestUtil.VALID_STUDENT_ID_AMY;
import static jeryl.fyp.logic.commands.CommandTestUtil.VALID_STUDENT_ID_BOB;
import static jeryl.fyp.logic.commands.CommandTestUtil.assertCommandFailure;
import static jeryl.fyp.logic.commands.CommandTestUtil.assertCommandSuccess;
import static jeryl.fyp.logic.commands.CommandTestUtil.showStudentAtIndex;
import static jeryl.fyp.testutil.TypicalIndexes.INDEX_FIRST_STUDENT;
import static jeryl.fyp.testutil.TypicalStudents.getTypicalFypManager;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jeryl.fyp.commons.core.Messages;
import jeryl.fyp.model.Model;
import jeryl.fyp.model.ModelManager;
import jeryl.fyp.model.UserPrefs;
import jeryl.fyp.model.student.Student;
import jeryl.fyp.model.student.StudentId;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteStudentCommand}.
 */
public class ListDeadlineCommandTest {

    private Model model = new ModelManager(getTypicalFypManager(), new UserPrefs());

    @Test
    public void execute_validStudentIdUnfilteredList_success() {
        Student studentToListDeadline = model.getFilteredStudentList().get(INDEX_FIRST_STUDENT.getZeroBased());
        StudentId validStudentId = studentToListDeadline.getStudentId();
        ListDeadlineCommand listDeadlineCommand = new ListDeadlineCommand(validStudentId);

        String expectedMessage = String.format(ListDeadlineCommand.MESSAGE_LIST_DEADLINE_SUCCESS, studentToListDeadline);

        ModelManager expectedModel = new ModelManager(model.getFypManager(), new UserPrefs());

        assertCommandSuccess(listDeadlineCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_nonexistentStudentIdUnfilteredList_throwsCommandException() {
        Student studentToListDeadline = model.getFilteredStudentList().get(INDEX_FIRST_STUDENT.getZeroBased());
        StudentId nonexistentStudentId = new StudentId(VALID_STUDENT_ID_AMY);
        ListDeadlineCommand listDeadlineCommand = new ListDeadlineCommand(nonexistentStudentId);

        ModelManager expectedModel = new ModelManager(model.getFypManager(), new UserPrefs());

        assertCommandFailure(listDeadlineCommand, model, Messages.MESSAGE_STUDENT_NOT_FOUND);
    }

    @Test
    public void execute_validStudentIdFilteredList_success() {
        showStudentAtIndex(model, INDEX_FIRST_STUDENT);

        Student studentToListDeadline = model.getFilteredStudentList().get(INDEX_FIRST_STUDENT.getZeroBased());
        StudentId validStudentId = studentToListDeadline.getStudentId();
        ListDeadlineCommand listDeadlineCommand = new ListDeadlineCommand(validStudentId);

        String expectedMessage = String.format(ListDeadlineCommand.MESSAGE_LIST_DEADLINE_SUCCESS, studentToListDeadline);

        ModelManager expectedModel = new ModelManager(model.getFypManager(), new UserPrefs());
        showNoStudent(expectedModel);

        assertCommandSuccess(listDeadlineCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_nonexistentStudentIdFilteredList_throwsCommandException() {
        showStudentAtIndex(model, INDEX_FIRST_STUDENT);

        Student studentToDelete = model.getFilteredStudentList().get(INDEX_FIRST_STUDENT.getZeroBased());
        StudentId nonexistentStudentId = new StudentId(VALID_STUDENT_ID_AMY);
        ListDeadlineCommand listDeadlineCommand = new ListDeadlineCommand(nonexistentStudentId);

//        ModelManager expectedModel = new ModelManager(model.getFypManager(), new UserPrefs());
//        expectedModel.deleteStudent(studentToDelete);

        assertCommandFailure(listDeadlineCommand, model, Messages.MESSAGE_STUDENT_NOT_FOUND);
    }

    @Test
    public void equals() {
        ListDeadlineCommand listDeadlineFirstCommand = new ListDeadlineCommand(new StudentId(VALID_STUDENT_ID_AMY));
        ListDeadlineCommand listDeadlineSecondCommand = new ListDeadlineCommand(new StudentId(VALID_STUDENT_ID_BOB));

        // same object -> returns true
        assertTrue(listDeadlineFirstCommand.equals(listDeadlineFirstCommand));

        // same values -> returns true
        DeleteStudentCommand deleteFirstCommandCopy = new DeleteStudentCommand(new StudentId(VALID_STUDENT_ID_AMY));
        assertTrue(listDeadlineFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(listDeadlineFirstCommand.equals(1));

        // null -> returns false
        assertFalse(listDeadlineFirstCommand.equals(null));

        // different student -> returns false
        assertFalse(listDeadlineFirstCommand.equals(listDeadlineSecondCommand));
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoStudent(Model model) {
        model.updateFilteredStudentList(p -> false);

        assertTrue(model.getFilteredStudentList().isEmpty());
    }
}
