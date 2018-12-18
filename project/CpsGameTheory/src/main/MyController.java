

package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.Publisher;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.DataReaderListener;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.ViewStateKind;
import com.rti.dds.topic.Topic;

import DataModule.CentralData;
import DataModule.CentralDataDataWriter;
import DataModule.CentralDataTypeSupport;
import DataModule.EnergyType;
import DataModule.UserConsumption;
import DataModule.UserConsumptionDataReader;
import DataModule.UserConsumptionDataWriter;
import DataModule.UserConsumptionSeq;
import DataModule.UserConsumptionTypeSupport;
import UserModule.RoleType;
import UserModule.UserStruct;
import UserModule.UserStructDataReader;
import UserModule.UserStructSeq;
import UserModule.UserStructTypeSupport;


// ===========================================================================

public class MyController {
    // -----------------------------------------------------------------------
    // Public Methods
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
    	
        // --- Get domain ID --- //
        int domainId = 0;
        if (args.length >= 1) {
            domainId = Integer.valueOf(args[0]).intValue();
        }

        // -- Get max loop count; 0 means infinite loop --- //
        int sampleCount = 0;
        if (args.length >= 2) {
            sampleCount = Integer.valueOf(args[1]).intValue();
        }

        /* Uncomment this to turn on additional logging
        Logger.get_instance().set_verbosity_by_category(
            LogCategory.NDDS_CONFIG_LOG_CATEGORY_API,
            LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
        */

        // --- Run --- //
        publisherMain(domainId, sampleCount);
    }

    // -----------------------------------------------------------------------
    // Private Methods
    // -----------------------------------------------------------------------
    private static List<Double> values = null;
    private static int index = 0;
    
    public static CentralData globalDataAcquire() {
    	if (values == null) {
	    	URL url = MyController.class.getResource("../prices.csv");
	    	
	    	File file= new File(url.toString().replace("file:/", ""));
	    	
	        // this gives you a 2-dimensional array of strings
	        values = new ArrayList<>();
	        Scanner inputStream;
	
	        try{
	            inputStream = new Scanner(file);
	            int i = 0;
	            while(inputStream.hasNextLine() && i++<10){
	                String line= inputStream.next();
	                // this adds the currently parsed line to the 2-dimensional string array
	                values.add(Double.valueOf(line.replace(',', '.')));
	            }
	
	            inputStream.close();
	        }catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }
    	}
    	
    	double price = values.get(index++);
    	
    	double[] array = new double[7];
    	for (int i = 0; i < 7; i++)
    		array[i] = price;
    	
    	CentralData create = (CentralData) CentralData.create();
    	create.prices = array;
    	create.timestamp = (int) System.currentTimeMillis();
    	
    	return create;
    }
    public static long registerUser(Long id) {
    	//placeholder, might be useful.
    	return 0;
    }
    
    public static void updateUsers(CentralDataDataWriter priceTopicWriter) {
        // For a data type that has a key, if the same instance is going to be
        // written multiple times, initialize the key here
        // and register the keyed instance prior to writing 
        // instance_handle = writer.register_instance(instance);
        CentralData instance = globalDataAcquire();
        InstanceHandle_t instance_handle = InstanceHandle_t.HANDLE_NIL;
        instance_handle = priceTopicWriter.register_instance(instance);
        priceTopicWriter.write(instance, instance_handle);
        priceTopicWriter.unregister_instance(instance, instance_handle);
    }
    
    public static int addClient(Long id, double[] con_value, double[] prod_value) {
    	UserStruct foundUser = users.get(id);
    	boolean isConsumer = false;
    	boolean isProducer = false;
		if (foundUser!= null) {
    		for (RoleType asd: foundUser.role) {
    			if (asd == RoleType.CONSUMER || asd == RoleType.CRITICAL_CONSUMER) {
    				isConsumer = true;
    			}
    			if (asd == RoleType.PROVIDER) {
    				isProducer = true;
    			}
    		}
    		if (help_array_check(consumptions.get(id).consumptions, 0.001) && (isConsumer == false)) {
    			RoleType[] newRoles = new RoleType[foundUser.role.length + 1];
    			int counter = 0;
    			for (RoleType dsa: foundUser.role) {
    				newRoles[counter] = dsa;
    				counter++;
    			}
    			newRoles[counter] = RoleType.CONSUMER;
    		}
    		if (help_array_check(consumptions.get(id).productions, 0.001) && (isProducer == false)) {
    			RoleType[] newRoles = new RoleType[foundUser.role.length + 1];
    			int counter = 0;
    			for (RoleType dsa: foundUser.role) {
    				newRoles[counter] = dsa;
    				counter++;
    			}
    			newRoles[counter] = RoleType.PROVIDER;
    		}
    	}
    	return 0;
    }
    
    public static HashMap<Long, Double[]> getOfferBuy(Long id_who, double value) {
    	UserStruct who = users.get(id_who);
    	currentClient = id_who;
    	HashMap<Long, Double[]> potentials = new HashMap<Long, Double[]>();
    	for (Long client: providers.keySet()) {
    		if (client != id_who) {
    			double[] potential = help_providers_potential(providers.get(client), consumptions.get(client).productions);
    			potentials.put(client, help_double_cast(potential));
    		}
    	}
    	HashMap<Long, Double[]> res = help_ideal_match(potentials, value);
    	for (Long agreed: res.keySet()) {
    		providers.get(agreed).replace(id_who, providers.get(agreed).get(id_who), help_double_cast(help_providers_add(help_double_lowercast(res.get(agreed)), help_double_lowercast(providers.get(agreed).get(id_who))))); 
    	}
    	for (Long agreed: res.keySet()) {
    		consumptions.get(id_who).productions = help_providers_add(consumptions.get(id_who).consumptions, help_double_lowercast(res.get(agreed)));
    	}
    	return res;
    }
    
    public static HashMap<Long, Double[]> getOfferSell(Long id_who, Double[] value) {
    	currentClient = id_who;
		HashMap<Long, Double[]> ideal_sale = help_ideal_sale(value);
		consumptions.get(id_who).productions = help_providers_add(help_double_lowercast(value), consumptions.get(id_who).productions);
		sellers++;
		for (Long agreed: ideal_sale.keySet()) {
    		providers.get(id_who).replace(agreed, providers.get(id_who).get(agreed), help_double_cast(help_providers_add(help_double_lowercast(ideal_sale.get(agreed)), help_double_lowercast(providers.get(id_who).get(agreed))))); 
    	}
		return ideal_sale;
    }
    
    public static int sendWarnings() {
    	if ((systemInflation > 1.1) || help_critical_consumer_need()) {
    		return -1;
    	}
    	return 0;
    }
    
    public static void monopolInflation() {
    	if (sellers < 3) {
    		systemInflation *= 1.1;
    		sendWarnings();
    	}
    }
    
    public static double getEnergyCost(EnergyType type) {
    	switch(type.value()) {
    		case 0: return 0.063;	//SOLAR
    		case 1: return 0.059;	//WIND
    		case 2: return 0.062;	//HYDRO
    		case 3: return 0.095;	//BIOMASS
    		case 4: return 0.093;	//NUCLEAR
    		case 5: return 0.12;	//COAL
    		case 6: return 0.05;	//GAS
    	}
		return 0.0;
    }
    
    private static boolean help_critical_consumer_need() {
    	for (Long id: unfulfilled.keySet()) {
    		for (RoleType that: users.get(id).role) {
    			if (that == RoleType.CRITICAL_CONSUMER)
    				return true;
    		}
    	}
    	return false;
    }
    
    private static HashMap<Long, Double[]> help_ideal_sale(Double[] value) {
    	HashMap<Long, Double[]> resStruct = new HashMap<Long, Double[]>();
    	ArrayList<Long> sortedUsersByDistance = new ArrayList<Long>();
    	
    	while (sortedUsersByDistance.size() != (unfulfilled.size() + 1)) {
    		double maximalDistance = 0.0;
    		Long toBeAdded = (long) 0;
    		for (Long user1: unfulfilled.keySet()) {
    			if (maximalDistance < users.get(user1).distance) {
    				maximalDistance = users.get(user1).distance;
    				toBeAdded = user1;
    			}
    		}
    		sortedUsersByDistance.add(toBeAdded);
    	}
    	Collections.reverse(sortedUsersByDistance);
    	for (Long user: sortedUsersByDistance) {
    		resStruct.put(user, new Double[7]);
    		if ((value[6] - unfulfilled.get(user) < 0.0)) {
    			resStruct.get(user)[6] = unfulfilled.get(user) - value[6];
    			value[6] = 0.0;
    		} else {
    			resStruct.get(user)[6] = value[6];
    			unfulfilled.replace(user, unfulfilled.get(user) - value[6]);
    			break;
    		}
    	}
    	for (Long user: sortedUsersByDistance) {
    		if ((value[1] - unfulfilled.get(user) < 0.0)) {
    			resStruct.get(user)[1] = unfulfilled.get(user) - value[1];
    			value[1] = 0.0;
    		} else {
    			resStruct.get(user)[1] = value[1];
    			unfulfilled.replace(user, unfulfilled.get(user) - value[1]);
    			break;
    		}
    	}
    	for (Long user: sortedUsersByDistance) {
    		if ((value[2] - unfulfilled.get(user) < 0.0)) {
    			resStruct.get(user)[2] = unfulfilled.get(user) - value[2];
    			value[2] = 0.0;
    		} else {
    			resStruct.get(user)[2] = value[2];
    			unfulfilled.replace(user, unfulfilled.get(user) - value[2]);
    			break;
    		}
    	}
    	for (Long user: sortedUsersByDistance) {
    		if ((value[0] - unfulfilled.get(user) < 0.0)) {
    			resStruct.get(user)[0] = unfulfilled.get(user) - value[0];
    			value[0] = 0.0;
    		} else {
    			resStruct.get(user)[0] = value[0];
    			unfulfilled.replace(user, unfulfilled.get(user) - value[0]);
    			break;
    		}
    	}
    	for (Long user: sortedUsersByDistance) {
    		if ((value[4] - unfulfilled.get(user) < 0.0)) {
    			resStruct.get(user)[4] = unfulfilled.get(user) - value[4];
    			value[4] = 0.0;
    		} else {
    			resStruct.get(user)[4] = value[4];
    			unfulfilled.replace(user, unfulfilled.get(user) - value[4]);
    			break;
    		}
    	}
    	for (Long user: sortedUsersByDistance) {
    		if ((value[3] - unfulfilled.get(user) < 0.0)) {
    			resStruct.get(user)[3] = unfulfilled.get(user) - value[3];
    			value[3] = 0.0;
    		} else {
    			resStruct.get(user)[3] = value[3];
    			unfulfilled.replace(user, unfulfilled.get(user) - value[3]);
    			break;
    		}
    	}
    	for (Long user: sortedUsersByDistance) {
    		if ((value[5] - unfulfilled.get(user) < 0.0)) {
    			resStruct.get(user)[5] = unfulfilled.get(user) - value[5];
    			value[5] = 0.0;
    		} else {
    			resStruct.get(user)[5] = value[5];
    			unfulfilled.replace(user, unfulfilled.get(user) - value[5]);
    			break;
    		}
    	}
    	return resStruct;
    	
    }
    
    private static Double[] help_double_cast(double[] toCast) {
    	Double[] res = new Double[7]; 
    	for (int i=0;i<7;i++) {
    		res[i] = toCast[i];
    	}
    	return res;
    }
    
    private static double[] help_double_lowercast(Double[] toCast) {
    	double[] res = new double[7];
    	for (int i=0; i<7; i++) {
    		res[i] = toCast[i];
    	}
    	return res;
    }
    
    private static boolean help_array_check(double[] toBeChecked, double threshold){
    	boolean allZero = true;
        for (double value: toBeChecked) {
        	if (value <= -threshold && value >= threshold) {
        		allZero = false;
        	}
        }
        return allZero;
    }
    
    private static double[] help_providers_current(HashMap<Long, Double[]> providerstruct) {
    	double[] res = new double[7];
    	for (Long oneClient: providerstruct.keySet()) {
    		for (int i= 0; i<7; i++) {
    			res[i] += providerstruct.get(oneClient)[i];
    		}
    	}
    	return res;
    }
    
    private static double help_ideal_cost(HashMap<Long, Double[]> idealMatch) {
    	double totalCost = 0.0;
    	for (Long user: idealMatch.keySet()) {
    		double distanceMod = 1.0 + users.get(user).distance/(100.0);
    		for (int i=0; i<7;i++) {
    			totalCost += (1.0 + getEnergyCost(EnergyType.valueOf(i))) * idealMatch.get(user)[i] * distanceMod;
    		}
    	}
    	return totalCost;
    }
    
    private static double[] help_providers_diff(double[] inUse, double[] total) {
    	double[] res = new double[7];
    	for (int i=0; i<7; i++) {
    		res[i] = total[i] - inUse[i];
    	}
    	return res;
    }
    
    private static double[] help_providers_add(double[] toAdd, double[] total) {
    	double[] res = new double[7];
    	for (int i=0; i<7; i++) {
    		res[i] = total[i] + toAdd[i];
    	}
    	return res;
    }
    
    private static double[] help_providers_potential(HashMap<Long, Double[]> providerStruct, double[] production) {
    	double[] current = help_providers_current(providerStruct);
    	return (help_providers_diff(current, production));
    }
    
    private static HashMap<Long, Double[]> help_ideal_match(HashMap<Long, Double[]> potentialsStruct, double needed) {
    	HashMap<Long, Double[]> resStruct = new HashMap<Long, Double[]>();
    	double satisfied = 0.0;
    	ArrayList<Long> sortedUsersByDistance = new ArrayList<Long>();
    	
    	while (sortedUsersByDistance.size() != (potentialsStruct.size() + 1)) {
    		double maximalDistance = 0.0;
    		Long toBeAdded = (long) 0;
    		for (Long user1: potentialsStruct.keySet()) {
    			if (maximalDistance < users.get(user1).distance) {
    				maximalDistance = users.get(user1).distance;
    				toBeAdded = user1;
    			}
    		}
    		sortedUsersByDistance.add(toBeAdded);
    	}
    	Collections.reverse(sortedUsersByDistance);
    	while (satisfied < needed) {
    		for (Long user: sortedUsersByDistance) {
    			resStruct.put(user, new Double[7]);
    			if ((needed - potentialsStruct.get(user)[6]) < 0.0) {
    				resStruct.get(user)[6] = potentialsStruct.get(user)[6] - needed;
    				needed = 0.0;
    				break;
    			} else {
    				resStruct.get(user)[6] = potentialsStruct.get(user)[6];
    				needed -= potentialsStruct.get(user)[6];
    			}
    		}
    		for (Long user: sortedUsersByDistance) {
    			if ((needed - potentialsStruct.get(user)[1]) < 0.0) {
    				resStruct.get(user)[1] = potentialsStruct.get(user)[1] - needed;
    				needed = 0.0;
    				break;
    			} else {
    				resStruct.get(user)[1] = potentialsStruct.get(user)[1];
    				needed -= potentialsStruct.get(user)[1];
    			}
    		}
    		for (Long user: sortedUsersByDistance) {
    			if ((needed - potentialsStruct.get(user)[2]) < 0.0) {
    				resStruct.get(user)[2] = potentialsStruct.get(user)[2] - needed;
    				needed = 0.0;
    				break;
    			} else {
    				resStruct.get(user)[2] = potentialsStruct.get(user)[2];
    				needed -= potentialsStruct.get(user)[2];
    			}
    		}
    		for (Long user: sortedUsersByDistance) {
    			if ((needed - potentialsStruct.get(user)[0]) < 0.0) {
    				resStruct.get(user)[0] = potentialsStruct.get(user)[0] - needed;
    				needed = 0.0;
    				break;
    			} else {
    				resStruct.get(user)[0] = potentialsStruct.get(user)[0];
    				needed -= potentialsStruct.get(user)[0];
    			}
    		}
    		for (Long user: sortedUsersByDistance) {
    			if ((needed - potentialsStruct.get(user)[4]) < 0.0) {
    				resStruct.get(user)[4] = potentialsStruct.get(user)[4] - needed;
    				needed = 0.0;
    				break;
    			} else {
    				resStruct.get(user)[4] = potentialsStruct.get(user)[4];
    				needed -= potentialsStruct.get(user)[4];
    			}
    		}
    		for (Long user: sortedUsersByDistance) {
    			if ((needed - potentialsStruct.get(user)[3]) < 0.0) {
    				resStruct.get(user)[3] = potentialsStruct.get(user)[3] - needed;
    				needed = 0.0;
    				break;
    			} else {
    				resStruct.get(user)[3] = potentialsStruct.get(user)[3];
    				needed -= potentialsStruct.get(user)[3];
    			}
    		}
    		for (Long user: sortedUsersByDistance) {
    			if ((needed - potentialsStruct.get(user)[5]) < 0.0) {
    				resStruct.get(user)[5] = potentialsStruct.get(user)[5] - needed;
    				needed = 0.0;
    				break;
    			} else {
    				resStruct.get(user)[5] = potentialsStruct.get(user)[5];
    				needed -= potentialsStruct.get(user)[5];
    			}
    		}
    	}
    	if (!(needed >= -0.001 && needed <= 0.001)) {
    		unfulfilled.put(currentClient, needed);
    	}
    	return resStruct;
    	
    }
    
    private static void help_providers_newcons(double[] toBeAllocated, Long who, Long to_whom) {
    	consumptions.get(to_whom).consumptions = help_providers_add(toBeAllocated, consumptions.get(to_whom).consumptions);
    	for (int i=0; i<7; i++) {
    		providers.get(who).get(to_whom)[i] = providers.get(who).get(to_whom)[i] + toBeAllocated[i];
    	}
    }
    
    private static int sellers = 0;
    private static Long currentClient;
    private static double systemInflation = 1.0;
    private static Map<Long, Double> unfulfilled = new HashMap<Long, Double>();
    private static Map<Long, HashMap<Long, Double[]>> providers = new HashMap<Long, HashMap<Long, Double[]>>();
    private static Map<Long, UserStruct> users = new HashMap<Long, UserStruct>();
    private static Map<Long, UserConsumption> consumptions = new HashMap<Long, UserConsumption>();
    
    // --- Constructors: -----------------------------------------------------

    private MyController() {
        super();
    }

    // -----------------------------------------------------------------------

    private static void publisherMain(int domainId, int sampleCount) {

    	users = new HashMap<Long, UserStruct>();;
        consumptions = new HashMap<Long, UserConsumption>();
    	
        DomainParticipant participant = null;
        Subscriber subscriber = null;
        Publisher publisher = null;
        Topic userTopic = null;
        Topic consumptionTopic = null;
        Topic priceTopic = null;
        Topic offerTopic = null;
        DataReaderListener userListener = null;
        DataReaderListener consumptionListener = null;
        CentralDataDataWriter priceTopicWriter = null;
        UserConsumptionDataReader consumptionTopicReader = null;
        UserStructDataReader userRegisterTopicReader = null;
        UserConsumptionDataWriter offerTopicWriter = null;

        
        try {
            // --- Create participant --- //

            /* To customize participant QoS, use
            the configuration file
            USER_QOS_PROFILES.xml */

            participant = DomainParticipantFactory.TheParticipantFactory.
            create_participant(
                domainId, DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                System.err.println("create_participant error\n");
                return;
            }                         

            // --- Create publisher --- //

            /* To customize publisher QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            publisher = participant.create_publisher(
                DomainParticipant.PUBLISHER_QOS_DEFAULT, null /* listener */,
                StatusKind.STATUS_MASK_NONE);
            if (publisher == null) {
                System.err.println("create_publisher error\n");
                return;
            }                   

            // --- Create subscriber --- //

            /* To customize subscriber QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            subscriber = participant.create_subscriber(
                DomainParticipant.SUBSCRIBER_QOS_DEFAULT, null /* listener */,
                StatusKind.STATUS_MASK_NONE);
            if (subscriber == null) {
                System.err.println("create_subscriber error\n");
                return;
            } 
            
            // --- Create topic --- //

            /* Register type before creating topic */
            String centralTypeName = CentralDataTypeSupport.get_type_name(); 
            CentralDataTypeSupport.register_type(participant, centralTypeName);
            String userTypeName = UserStructTypeSupport.get_type_name(); 
            UserStructTypeSupport.register_type(participant, userTypeName);
            String consumptionTypeName = UserConsumptionTypeSupport.get_type_name(); 
            UserConsumptionTypeSupport.register_type(participant, centralTypeName);

            /* To customize topic QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            userTopic = participant.create_topic(
                "UserRegister",
                userTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (userTopic == null) {
                System.err.println("create_topic error\n");
                return;
            }                     

            consumptionTopic = participant.create_topic(
                "Consumption",
                consumptionTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (consumptionTopic == null) {
                System.err.println("create_topic error\n");
                return;
            }                     

            priceTopic = participant.create_topic(
                "Price",
                centralTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (priceTopic == null) {
                System.err.println("create_topic error\n");
                return;
            }                     

            offerTopic = participant.create_topic(
                "Offer",
                consumptionTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (offerTopic == null) {
                System.err.println("create_topic error\n");
                return;
            }                     

            // --- Create writer --- //

            /* To customize data writer QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            priceTopicWriter = (CentralDataDataWriter)
            publisher.create_datawriter(
                priceTopic, Publisher.DATAWRITER_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (priceTopicWriter == null) {
                System.err.println("create_datawriter error\n");
                return;
            }           

            offerTopicWriter = (UserConsumptionDataWriter)
            publisher.create_datawriter(
                offerTopic, Publisher.DATAWRITER_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (offerTopicWriter == null) {
                System.err.println("create_datawriter error\n");
                return;
            }           

            // --- Create reader --- //

           // listener = new Listener(dbHandler); TODO for visualization
            consumptionListener = new ConsumptionListener();
            userListener = new UserListener();

            /* To customize data reader QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            consumptionTopicReader = (UserConsumptionDataReader)
            subscriber.create_datareader(
                consumptionTopic, Subscriber.DATAREADER_QOS_DEFAULT, consumptionListener,
                StatusKind.STATUS_MASK_ALL);
            if (consumptionTopicReader == null) {
                System.err.println("create_datareader error\n");
                return;
            }                         

            userRegisterTopicReader = (UserStructDataReader)
            subscriber.create_datareader(
                userTopic, Subscriber.DATAREADER_QOS_DEFAULT, userListener,
                StatusKind.STATUS_MASK_ALL);
            if (userRegisterTopicReader == null) {
                System.err.println("create_datareader error\n");
                return;
            }                         

            // --- Write --- //

            final long sendPeriodMillis = 60 * 1000; // minutes
            long lastSentPrice = 0;
            while (true) {
            	if (Math.abs(lastSentPrice - System.currentTimeMillis()) > sendPeriodMillis) {
            		updateUsers(priceTopicWriter);
            	}
            	
            	sendProposals(offerTopicWriter);
            	
            	try {
					Thread.sleep(100); // Not real time decision, wait for some other consumptions
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
            }
            
           
        } finally {

            // --- Shutdown --- //

            if(participant != null) {
                participant.delete_contained_entities();

                DomainParticipantFactory.TheParticipantFactory.
                delete_participant(participant);
            }
            /* RTI Data Distribution Service provides finalize_instance()
            method for people who want to release memory used by the
            participant factory singleton. Uncomment the following block of
            code for clean destruction of the participant factory
            singleton. */
            //DomainParticipantFactory.finalize_instance();
        }
    }
    
    public static void sendProposals(UserConsumptionDataWriter offerTopicWriter) {
    	for (long user : users.keySet()) {
    		if (consumptions.containsKey(user)) {
    			UserConsumption instance = consumptions.get(user);
    			InstanceHandle_t instance_handle = InstanceHandle_t.HANDLE_NIL;
    			instance_handle = offerTopicWriter.register_instance(instance);
    			offerTopicWriter.write(instance, instance_handle);
    			offerTopicWriter.unregister_instance(instance, instance_handle);
    		}
    	}
    }
    
private static class UserListener extends DataReaderAdapter {
    	
        UserStructSeq _dataSeq = new UserStructSeq();
        SampleInfoSeq _infoSeq = new SampleInfoSeq();
    	 
        public void on_data_available(DataReader reader) {

            UserStructDataReader userReader = (UserStructDataReader)reader;

            try {
            	userReader.take(
                    _dataSeq, _infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

                for(int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = (SampleInfo)_infoSeq.get(i);

                    if (info.valid_data) {
                        //System.out.println(
                         //   ((UserStruct)_dataSeq.get(i)).toString("Received",0));
                    	UserStruct registeredUser = (UserStruct)_dataSeq.get(i);
                    	long registeredID = registerUser(registeredUser.id);
                        users.put(registeredID, registeredUser);
                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
                userReader.return_loan(_dataSeq, _infoSeq);
            }
        }
    }

private static class ConsumptionListener extends DataReaderAdapter {
	
    UserConsumptionSeq _dataSeq = new UserConsumptionSeq();
    SampleInfoSeq _infoSeq = new SampleInfoSeq();
	 
    public void on_data_available(DataReader reader) {

        UserConsumptionDataReader consumptionReader = (UserConsumptionDataReader)reader;

        try {
        	consumptionReader.take(
                _dataSeq, _infoSeq,
                ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                SampleStateKind.ANY_SAMPLE_STATE,
                ViewStateKind.ANY_VIEW_STATE,
                InstanceStateKind.ANY_INSTANCE_STATE);

            for(int i = 0; i < _dataSeq.size(); ++i) {
                SampleInfo info = (SampleInfo)_infoSeq.get(i);

                if (info.valid_data) {
                    //System.out.println(
                    //    ((UserConsumption)_dataSeq.get(i)).toString("Received",0));
                    UserConsumption newConsumption = (UserConsumption)_dataSeq.get(i);
                    consumptions.put(newConsumption.userid, newConsumption);
                    HashMap<Long, Double[]> startingOut = new HashMap<Long, Double[]>();
                    providers.put(newConsumption.userid, startingOut);
                    addClient(newConsumption.userid, newConsumption.consumptions, newConsumption.productions);
                }
            }
        } catch (RETCODE_NO_DATA noData) {
            // No data to process
        } finally {
            consumptionReader.return_loan(_dataSeq, _infoSeq);
        }
    }
}
}

