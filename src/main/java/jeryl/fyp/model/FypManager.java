package jeryl.fyp.model;

import static java.util.Objects.requireNonNull;
import static jeryl.fyp.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javafx.collections.ObservableList;
import jeryl.fyp.commons.core.index.Index;
import jeryl.fyp.logic.commands.EditCommand;
import jeryl.fyp.model.student.Deadline;
import jeryl.fyp.model.student.Email;
import jeryl.fyp.model.student.ProjectName;
import jeryl.fyp.model.student.ProjectStatus;
import jeryl.fyp.model.student.Student;
import jeryl.fyp.model.student.StudentId;
import jeryl.fyp.model.student.StudentName;
import jeryl.fyp.model.student.UniqueStudentList;
import jeryl.fyp.model.tag.Tag;

/**
 * Wraps all data at the FYP-manager level
 * Duplicates are not allowed (by .isSameStudent comparison)
 */
public class FypManager implements ReadOnlyFypManager {

    private final UniqueStudentList students;
    /*
     * The 'unusual' code block below is a non-static initialization block, sometimes used to avoid duplication
     * between constructors. See https://docs.oracle.com/javase/tutorial/java/javaOO/initial.html
     *
     * Note that non-static init blocks are not recommended to use. There are other ways to avoid duplication
     *   among constructors.
     */
    {
        students = new UniqueStudentList();
    }

    public FypManager() {}

    /**
     * Creates a FypManager using the Students in the {@code toBeCopied}
     */
    public FypManager(ReadOnlyFypManager toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the student list with {@code students}.
     * {@code students} must not contain duplicate students.
     */
    public void setStudents(List<Student> students) {
        this.students.setStudents(students);
    }

    /**
     * Resets the existing data of this {@code FypManager} with {@code newData}.
     */
    public void resetData(ReadOnlyFypManager newData) {
        requireNonNull(newData);

        setStudents(newData.getStudentList());
    }

    //// student-level operations

    /**
     * Returns true if a student with the same identity as {@code student} exists in the FYP manager.
     */
    public boolean hasStudent(Student student) {
        requireNonNull(student);
        return students.contains(student);
    }

    /**
     * Adds a student to the FYP manager.
     * The student must not already exist in the FYP manager.
     */
    public void addStudent(Student p) {
        students.add(p);
    }

    /**
     * Replaces the given student {@code target} in the list with {@code editedStudent}.
     * {@code target} must exist in the FYP manager.
     * The student identity of {@code editedStudent} must not be the same as another existing student
     * in the FYP manager.
     */
    public void setStudent(Student target, Student editedStudent) {
        requireNonNull(editedStudent);

        students.setStudent(target, editedStudent);
    }

    /**
     * Removes {@code student} from this {@code FypManager}.
     * {@code student} must exist in the FYP manager.
     */
    public void removeStudent(Student student) {
        students.remove(student);
    }

    /**
     * @param target the target student that we want change fields for
     * @param editStudentDescriptor parameters to be changed to
     */
    public void editStudents(Student target, EditCommand.EditStudentDescriptor editStudentDescriptor) {
        assert target != null;

        StudentName updatedStudentName = editStudentDescriptor.getName().orElse(target.getStudentName());
        StudentId updatedStudentId = editStudentDescriptor.getStudentId().orElse(target.getStudentId());
        Email updatedEmail = editStudentDescriptor.getEmail().orElse(target.getEmail());
        ProjectName updatedProjectName = editStudentDescriptor.getProjectName().orElse(target.getProjectName());
        ProjectStatus updatedProjectStatus =
                target.getProjectStatus(); //edit does not allow editing of project status
        Set<Tag> updatedTags = editStudentDescriptor.getTags().orElse(target.getTags());

        Student updatedStudent = new Student(updatedStudentName, updatedStudentId, updatedEmail,
                updatedProjectName, updatedProjectStatus, updatedTags);
        students.setStudent(target, updatedStudent);
    }

    /**
     * Returns unique Student if {@code students} contains the student with the specified studentId.
     */
    public Student getStudentByStudentId(StudentId studentId) {
        requireNonNull(studentId);

        return students.getStudentByStudentId(studentId);
    }

    /**
     * @param studentId studentId we provide to get index in FYPManager
     * @return index position in FYPManager
     */
    public Index getIndexByStudentId(StudentId studentId) {
        requireNonNull(studentId);

        return students.getIndexByStudentId(studentId);
    }

    //// deadline-level operations
    /**
     * Returns true if a deadline with the same identity as {@code deadline} exists in the {@code student}.
     */
    public boolean hasDeadline(Student student, Deadline deadline) {
        requireAllNonNull(student, deadline);
        return student.getDeadlineList().contains(deadline);
    }
    /**
     * Removes {@code deadline} from this specified {@code student}.
     * {@code deadline} must exist in the specified {@code student}.
     */
    public void removeDeadline(Student student, Deadline deadline) {
        student.getDeadlineList().remove(deadline);
        setStudent(student, student);
    }


    /**
     * Adds a deadline to this specified {@code student}.
     * The deadline must not already exist in this specified {@code student}.
     */
    public void addDeadline(Student student, Deadline deadline) {
        student.getDeadlineList().add(deadline);
        setStudent(student, student);
    }
    /**
     * Replaces the given deadline {@code target} in the list with {@code editedDeadline}.
     * {@code target} must exist in the {@code student}.
     * The deadline identity of {@code editedDeadline} must not be the same as another existing deadline
     * in the {@code student}.
     */
    public void setDeadline(Student student, Deadline target, Deadline editedDeadline) {
        requireAllNonNull(student, target, editedDeadline);
        student.getDeadlineList().setDeadline(target, editedDeadline);
        setStudent(student, student);
    }

    /**
     * Sorts our Uncompleted student list by specialisation (which naturally sorts it by alphabetical order as well)
     */
    public ObservableList<Student> getSortedBySpecialisationUncompletedStudentList() {
        return getUncompletedStudentList().sorted((Student a, Student b) -> a.getProjectName().toString()
                .toLowerCase().compareTo(b.getProjectName().toString().toLowerCase()));
    }

    /**
     * Sorts our Uncompleted student list by project Status(YTS, IP then DONE) then by alphabetical order
     */
    public ObservableList<Student> getSortedByProjectStatusUncompletedStudentList() {
        return getUncompletedStudentList().sorted(new Comparator<Student>() {

            public int compare(Student a, Student b) {
                int statusComp = b.getProjectStatus().toString().toLowerCase()
                        .compareTo(a.getProjectStatus().toString().toLowerCase());

                if (statusComp != 0) {
                    return statusComp;
                }

                return a.getProjectName().toString().toLowerCase()
                        .compareTo(b.getProjectName().toString().toLowerCase());
            }
        });
    }

    /**
     * Sorts our Completed student list by specialisation, which naturally sorts it in alphabetical order
     */
    public ObservableList<Student> getSortedCompletedStudentList() {
        return getCompletedStudentList().sorted((Student a, Student b) -> a.getProjectName().toString()
                .toLowerCase().compareTo(b.getProjectName().toString().toLowerCase()));
    }


    //// util methods

    @Override
    public String toString() {
        return students.asUnmodifiableObservableList().size() + " students";
        // TODO: refine later
    }

    @Override
    public ObservableList<Student> getStudentList() {
        return students.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Student> getUncompletedStudentList() {
        return students.filter(student -> !student.getProjectStatus().projectStatus.equals("DONE"));
    }

    @Override
    public ObservableList<Student> getCompletedStudentList() {
        return students.filter(student -> student.getProjectStatus().projectStatus.equals("DONE"));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof FypManager // instanceof handles nulls
                && students.equals(((FypManager) other).students));
    }

    @Override
    public int hashCode() {
        return students.hashCode();
    }
}
