package tlb.balancer;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.*;
import tlb.service.Server;
import tlb.splitter.correctness.ValidationResult;

/**
 * @understands checking if all partitions of a unique job-name + job-version + module_name combination have executed
 */
public class AllPartitionsExecutedAssertion extends Resource {

    private Server server;

    public AllPartitionsExecutedAssertion(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        setReadable(true);
        setModifiable(false);
        server = (Server) context.getAttributes().get(TlbClient.TALK_TO_SERVICE);
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        ValidationResult validationResult = null;
        try {
            validationResult = server.verifyAllPartitionsExecutedFor(RequestUtil.moduleName(getRequest()));
        } catch (UnsupportedOperationException e) {
            getResponse().setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
            return new StringRepresentation(e.getMessage());
        }
        if (validationResult.hasFailed()) {
            getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
        }
        return new StringRepresentation(validationResult.getMessage());
    }

    @Override
    public boolean allowOptions() {
        return false;
    }

    @Override
    public boolean allowHead() {
        return false;
    }
}
