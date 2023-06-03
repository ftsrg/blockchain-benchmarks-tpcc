package notes.chaincode.ftsrg.mit.bme.hu;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;

@Contract(
    name = "notes",
    info = @Info(
	title = "Notes",
        description = "Store notes identified by a title",
        version = "0.1.0-SNAPSHOT",
        license = @License(
            name = "Apache 2.0 License",
            url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
        contact = @Contact(
            email = "bpeter@edu.bme.hu",
            name = "Bertalan Zoltán Péter",
            url = "http://mit.bme.hu/~bpeter")))
@Default
public final class Chaincode implements ContractInterface {

    private enum Error {
        NOT_FOUND,
        ALREADY_EXISTS
    }


    //@ requires !title.equals("forbidden");
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String CreateNote(final Context ctx, final String title, final String content)
    {
        ChaincodeStub stub = ctx.getStub();

        if (NoteExists(ctx, title)) {
            String errorMessage = String.format("Note with title '%s' already exists", title);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, Error.ALREADY_EXISTS.toString());
        }

        stub.putStringState(title, content);
        return title;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String ReadNote(final Context ctx, final String title)
    {
        ChaincodeStub stub = ctx.getStub();
        String note = stub.getStringState(title);

        if (note == null || note.isEmpty()) {
            String errorMessage = String.format("Note with title '%s' does not exist", title);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, Error.NOT_FOUND.toString());
        }

	return note;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteNote(final Context ctx, final String title)
    {
        ChaincodeStub stub = ctx.getStub();

        if (!NoteExists(ctx, title)) {
            String errorMessage = String.format("Note with title '%s' does not exist", title);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, Error.NOT_FOUND.toString());
        }

        stub.delState(title);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean NoteExists(final Context ctx, final String title)
    {
        ChaincodeStub stub = ctx.getStub();
        String note = stub.getStringState(title);

        return (note != null && !note.isEmpty());
    }

}
