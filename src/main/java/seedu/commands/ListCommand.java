package seedu.commands;

import seedu.data.CarparkList;
import seedu.parser.search.Sentence;

public class ListCommand extends Command {
    public static final String COMMAND_WORD = "list";

    private final CarparkList carparkList;

    public ListCommand(CarparkList carparkList) {
        this.carparkList = carparkList;
    }
    @Override
    public CommandResult execute() {
        String result = carparkList.toString();
        return new CommandResult(result);
    }
}
