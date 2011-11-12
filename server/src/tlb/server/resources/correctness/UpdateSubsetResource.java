package tlb.server.resources.correctness;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import tlb.domain.SuiteNameCountEntry;
import tlb.server.repo.SetRepo;
import tlb.server.repo.model.SubsetCorrectnessChecker;

import java.io.IOException;

/**
 * @understands checking subsets against universal set data for correctness
 */
public class UpdateSubsetResource extends SetResource {

    public static final SuiteNameCountEntry.SuiteNameCountEntryComparator NAME_ENTRY_COMPARATOR = new SuiteNameCountEntry.SuiteNameCountEntryComparator();
    private SubsetCorrectnessChecker subsetCorrectnessChecker;

    public UpdateSubsetResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    protected void createRepos() throws IOException, ClassNotFoundException {
        super.createRepos();
        subsetCorrectnessChecker = new SubsetCorrectnessChecker(universalSetRepo);
    }

    @Override
    public void acceptRepresentation(Representation entity) throws ResourceException {
        if (! universalSetRepo.isPrimed()) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
            getResponse().setEntity(new StringRepresentation("Universal set for given job-name, job-version and module-name combination doesn't exist."));
            return;
        }
        SetRepo.OperationResult operationResult = universalSetRepo.usedBySubset(reqPayload(entity), 1, 2);
        getResponse().setStatus(Status.SUCCESS_OK);
    }
}