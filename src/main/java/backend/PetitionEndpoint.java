// package foo;
// import java.util.Date;
// import java.util.HashSet;
// import java.util.Iterator;
// import java.util.List;
// import java.util.Random;

// import com.google.api.server.spi.auth.common.User;
// import com.google.api.server.spi.config.Api;
// import com.google.api.server.spi.config.ApiMethod;
// import com.google.api.server.spi.config.ApiMethod.HttpMethod;
// import com.google.api.server.spi.config.ApiNamespace;
// import com.google.api.server.spi.config.Named;
// import com.google.api.server.spi.config.Nullable;
// import com.google.api.server.spi.response.CollectionResponse;
// import com.google.api.server.spi.response.UnauthorizedException;
// import com.google.api.server.spi.auth.EspAuthenticator;

// import com.google.appengine.api.datastore.Cursor;
// import com.google.appengine.api.datastore.DatastoreService;
// import com.google.appengine.api.datastore.DatastoreServiceFactory;
// import com.google.appengine.api.datastore.Entity;
// import com.google.appengine.api.datastore.FetchOptions;
// import com.google.appengine.api.datastore.Key;
// import com.google.appengine.api.datastore.Query;
// import com.google.appengine.api.datastore.PreparedQuery;
// import com.google.appengine.api.datastore.PropertyProjection;
// import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
// import com.google.appengine.api.datastore.Query.CompositeFilter;
// import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
// import com.google.appengine.api.datastore.Query.Filter;
// import com.google.appengine.api.datastore.Query.FilterOperator;
// import com.google.appengine.api.datastore.Query.FilterPredicate;
// import com.google.appengine.api.datastore.Query.SortDirection;
// import com.google.appengine.api.datastore.QueryResultList;
// import com.google.appengine.api.datastore.Transaction;

// @Api(name = "myApi",
//      version = "v1",
//      audiences = "1086302335339-m9ef92cv85rfgi6pb872ii1onead5504.apps.googleusercontent.com",
//   	 clientIds = {"1086302335339-m9ef92cv85rfgi6pb872ii1onead5504.apps.googleusercontent.com",
//         "1086302335339-m9ef92cv85rfgi6pb872ii1onead5504.apps.googleusercontent.com"},
//      namespace =
//      @ApiNamespace(
// 		   ownerDomain = "helloworld.example.com",
// 		   ownerName = "helloworld.example.com",
// 		   packagePath = "")
//      )




// //@Api(name = "petitionApi", version = "v1")
// public class PetitionEndpoint {

//     @ApiMethod(name = "infoUser", httpMethod = HttpMethod.GET)
//     public User InfoUser(User user) throws UnauthorizedException {
//         if (user == null) {
//             throw new UnauthorizedException("Invalid credentials");
//         }
//         System.out.println("User Info:" + user.toString());
//         return user;
//     }
    

//     @ApiMethod(name = "addPetition", httpMethod = HttpMethod.POST)
//     public Entity addPetition(PostPetition petition) {
    
//         Entity e = new Entity("Petition"); // Création d'une entité pour la pétition
//         e.setProperty("title", petition.title);
//         e.setProperty("description", petition.description);
//         e.setProperty("creatorId", petition.creatorId);
//         e.setProperty("status", petition.status);
//         e.setProperty("tags", petition.tags);
//         e.setProperty("creationDate", new Date()); // Date de création de la pétition
    
//         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
//         Transaction txn = datastore.beginTransaction();
//         datastore.put(e); // Stockage de l'entité de la pétition dans Datastore
//         txn.commit();
    
//         return e; // Retourne l'entité de la pétition nouvellement ajoutée
//     }
    

// }
