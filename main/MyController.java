

package main;

import java.util.HashMap;
import java.util.Map;

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
import DataModule.UserConsumption;
import DataModule.UserConsumptionDataReader;
import DataModule.UserConsumptionDataWriter;
import DataModule.UserConsumptionSeq;
import DataModule.UserConsumptionTypeSupport;
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

    public static long registerUser(Long id) {
    	//placeholder, might be useful.
    	return 0;
    }
    
    public static int updateUsers() {
    	return 0;
    }
    
    public static int addClient(Long id, double[] con_value, double[] prod_value) {
    	return 0;
    }
    
    public static HashMap<Long, Double> getOffer(Long id_who, double value, boolean direction) {
    	return new HashMap<Long, Double>();
    }
    
    public static int sendWarnings() {
    	return 0;
    }
    
    public static void monopolInflation() {
    }
    
    // -----------------------------------------------------------------------
    // Private Methods
    // -----------------------------------------------------------------------
    
    private double systemInflation = 1.0;
    private static Map<Long, HashMap<Long, Double[]>> providers = new HashMap<Long, HashMap<Long, Double[]>>();
    private static Map<Long, UserStruct> users = new HashMap<Long, UserStruct>();;
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

            /*
            //  TODO ???
             
            // Create data sample for writing 
            CentralData instance = new CentralData();

            InstanceHandle_t instance_handle = InstanceHandle_t.HANDLE_NIL;
            // For a data type that has a key, if the same instance is going to be
            // written multiple times, initialize the key here
            // and register the keyed instance prior to writing 
            // instance_handle = writer.register_instance(instance);

            final long sendPeriodMillis = 4 * 1000; // 4 seconds

            Random r = new Random();
            instance.ID = "temp0";
            
            for (int count = 0;
            (sampleCount == 0) || (count < sampleCount);
            ++count) {
                System.out.println("Writing Temperature, count " + count);

                // Modify the instance to be written here 

                instance.Value = r.nextDouble()*100;
                
                // Write data 
                priceTopicWriter.write(instance, instance_handle);
                try {
                    Thread.sleep(sendPeriodMillis);
                } catch (InterruptedException ix) {
                    System.err.println("INTERRUPTED");
                    break;
                }
            }

            */
            
            //writer.unregister_instance(instance, instance_handle);

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

