package tlb.server.resources;

import tlb.domain.Entry;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import tlb.server.repo.EntryRepo;
import tlb.server.repo.VersioningEntryRepo;

import java.io.IOException;
import java.util.Collection;

/**
 * @understands accessing and listing out data from versioned repo
 */
public abstract class VersionedResource<T extends VersioningEntryRepo> extends SimpleCRUResource<T> {
    public VersionedResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public final boolean allowPost() {
        return false;
    }

    @Override
    public final boolean allowPut() {
        return false;
    }

    @Override
    protected Entry parseEntry(Representation entity) throws IOException {
        throw new UnsupportedOperationException("parsing does not make sense, as mutation of versioned data is not allowed");
    }

    @Override
    protected Collection getListing() throws IOException, ClassNotFoundException {
        return repo.list(reqVersion());
    }
}
