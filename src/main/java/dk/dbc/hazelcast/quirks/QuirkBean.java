package dk.dbc.hazelcast.quirks;

import fish.payara.cdi.jsr107.impl.NamedCache;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.cache.Cache;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/")
@Stateless
public class QuirkBean {

    private static AtomicInteger i = new AtomicInteger();

    @Inject
    @NamedCache(cacheName = "c1")
    Cache<String, Set<String>> c1;

    @Inject
    @NamedCache(cacheName = "c2")
    Cache<Set<String>, String> c2;

    @GET
    @Path("{b : (true|false)}")
    public Response quirk(@PathParam("b") boolean copy) {

        Set<String> mySet = c1.get("MY_KEY");
        if (mySet == null) {
            mySet = new HashSet<>(Arrays.asList("a", "b", "c"));
            c1.put("MY_KEY", mySet);
        }
        System.out.println("mySet(c2-key) = " + mySet);

        if (copy)
            mySet = new HashSet<>(mySet);

        System.out.println("cachedValue = " + c2.get(mySet));

        String value = "Persisted-" + i.incrementAndGet();
        c2.put(mySet, value);
        System.out.println("value stored = " + value);

        logCacheContent();

        return Response.ok(Instant.now().toString()).build();
    }

    private void logCacheContent() {
        try {
            Set<String> oldKey = null;
            String oldValue = null;
            for (Iterator<Cache.Entry<Set<String>, String>> iterator = c2.iterator() ; iterator.hasNext() ;) {
                Cache.Entry<Set<String>, String> next = iterator.next();
                Set<String> currentKey = next.getKey();
                String currentValue = next.getValue();
                System.out.println("currentKey = " + currentKey + "; " +
                                   "currentKey.hashCode() = " + currentKey.hashCode() + "; " +
                                   "currentKey.equals(oldKey) = " + currentKey.equals(oldKey));
                System.out.println("currentValue = " + currentValue + "; " +
                                   "currentValue.hashCode() = " + currentValue.hashCode() + "; " +
                                   "currentValue.equals(oldValue) = " + currentValue.equals(oldValue));
                if(iterator.hasNext())
                    System.out.println("--------");
                oldKey = currentKey;
                oldValue = currentValue;
            }
        } catch (Exception ex) {
            Logger.getLogger(QuirkBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
