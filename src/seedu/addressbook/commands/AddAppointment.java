package seedu.addressbook.commands;

import seedu.addressbook.data.exception.IllegalValueException;
import seedu.addressbook.common.Messages;
import seedu.addressbook.data.person.Person;
import seedu.addressbook.data.person.ReadOnlyPerson;
import seedu.addressbook.data.person.Schedule;
import seedu.addressbook.data.person.UniquePersonList;

import java.util.*;

import static seedu.addressbook.ui.Gui.DISPLAYED_INDEX_OFFSET;

public class AddAppointment extends Command{

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ":\n"
            + "Add in an appointment date for the selected person.\n"
            + "Note that multiple appointment dates are accepted\n\t"
            + "Parameters: DD-MM-YYYY...\n\t"
            + "Example 1: " + COMMAND_WORD + " 01-01-2019\n\t"
            + "Example 2: " + COMMAND_WORD + " 01-01-2019" + " 01-02-2019" + " 01-03-2019";

    private static final String MESSAGE_NO_CHANGE_MADE = "No changes made to the %1$s's set of appointment(s) "
            + "as appointment date(s) on %2$s are already recorded";

    private static final String MESSAGE_ADDED_PERSON_APPOINTMENT = "%1$s has new appointment date(s)\n";

    private static final String MESSAGE_FOR_ADDED_APPOINTMENTS = "\nAdded appointments on: %1$s\n";

    private static final String MESSAGE_FOR_DUPLICATE_APPOINTMENTS = "Appointments that already exist: %1$s";

    private final Set<Schedule> scheduleSetToAdd;

    private final String inputForHistory;


    /**
     * Convenience constructor using raw values.
     *
     * @throws IllegalValueException if any of the raw values are invalid
     */
    public AddAppointment(Set<String> schedule) throws IllegalValueException{

        final Set<Schedule> scheduleSet = new HashSet<>();
        for (String scheduleDate : schedule) {
            scheduleSet.add(new Schedule(scheduleDate));
        }
        this.scheduleSetToAdd = scheduleSet;

        inputForHistory = String.join(" ", schedule);
    }

    @Override
    public CommandResult execute() {
        try {
            saveHistory("(edit-appointment " + checkEditingPersonIndex() + ") " + COMMAND_WORD + " " + inputForHistory);
            this.setTargetIndex(checkEditingPersonIndex());
            final ReadOnlyPerson target = getTargetPerson();
            Set<Schedule> scheduleSet = target.getSchedules();

            String detailsMessage = getDetailedMessage(scheduleSet, target.getName().toString());
            boolean hasChanges = scheduleSet.addAll(scheduleSetToAdd);

            if (!hasChanges) { //check for changes
                return new CommandResult(String.format(MESSAGE_NO_CHANGE_MADE, target.getName(), inputForHistory));
            }

            Person updatedPerson = new Person(target);
            updatedPerson.setSchedule(scheduleSet);
            addressBook.editPerson(target, updatedPerson);

            List<ReadOnlyPerson> editablePersonList = this.getEditableLastShownList();
            editablePersonList.set(checkEditingPersonIndex() - DISPLAYED_INDEX_OFFSET, updatedPerson);
            return new CommandResult(detailsMessage, editablePersonList, editablePersonList, false);

        } catch (IndexOutOfBoundsException ie) {
            return new CommandResult(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        } catch (UniquePersonList.PersonNotFoundException pnfe) {
            return new CommandResult(Messages.MESSAGE_PERSON_NOT_IN_ADDRESSBOOK);
        } catch (NullPointerException nu) {
            return new CommandResult("Null pointer: Error in executing result. Report to developers.");
        }catch (UnsupportedOperationException nu) {
            return new CommandResult("Unsupported Operation: Error executing result. Report to developers.");
        }catch (ClassCastException nu) {
            return new CommandResult("Class Cast: Error executing result. Report to developers.");
        }
    }

    /**
     * Constructs a feedback message that details the effects of the input appointment dates.
     *
     * @param initialScheduleSet the original schedule of the user
     * @param name the name tobe printed in the message
     * @return a message that shows which appointments are added
     * and which appointments are not (as they are duplicated) for the person
     */
    private String getDetailedMessage(Set<Schedule> initialScheduleSet, String name){
        StringBuilder addedAppointments = new StringBuilder();
        StringBuilder duplicateAppointments = new StringBuilder();

        for(Schedule scheduleAdd : scheduleSetToAdd) {
            if(initialScheduleSet.contains(scheduleAdd)){
                duplicateAppointments.append(scheduleAdd.toString());
                duplicateAppointments.append(" ");
            }else{
                addedAppointments.append(scheduleAdd.toString());
                addedAppointments.append(" ");
            }
        }
        String detailsMessage = String.format(MESSAGE_ADDED_PERSON_APPOINTMENT, name);
        detailsMessage +=  String.format(MESSAGE_FOR_ADDED_APPOINTMENTS, addedAppointments);
        if (duplicateAppointments.length() > 0) detailsMessage += String.format(MESSAGE_FOR_DUPLICATE_APPOINTMENTS, duplicateAppointments);

        return detailsMessage;
    }

}