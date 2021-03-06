package org.jboss.injection.inject.naming;

import org.jboss.injection.inject.Injector;
import org.jboss.logging.Logger;

import javax.naming.Context;
import java.util.List;

/**
 * Populates a Context based on a set of Injectors
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class SwitchBoardOperator
{
   private static final Logger log = Logger.getLogger(SwitchBoardOperator.class);
   
   private final Context context;
   private final List<Injector<Context>> injectors;

   /**
    * Create with a context and set of injectors
    *
    * @param context The context to populate
    * @param injectors Injectors used to populate the context
    */
   public SwitchBoardOperator(final Context context, final List<Injector<Context>> injectors)
   {
      if(context == null) throw new IllegalArgumentException("Context can not be null");
      if(injectors == null) throw new IllegalArgumentException("Injectors must not be null");

      this.context = context;
      this.injectors = injectors;
   }

   /**
    * Called when this bean's dependencies are met.
    */
   public void start()
   {
      log.debugf("Executing %s", this);
      for(Injector<Context> injection : injectors)
      {
         injection.inject(context);
      }
   }

   @Override
   public String toString()
   {
      return "SwitchBoardOperator{" + "context=" + context + ", injectors=" + injectors + '}';
   }
}
