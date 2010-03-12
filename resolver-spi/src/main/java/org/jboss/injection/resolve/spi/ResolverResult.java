/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.injection.resolve.spi;

/**
 * The results of executing a Resolver.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class ResolverResult {
   private String jndiName;
   private String refName;
   private String beanName;

   @Deprecated
   protected ResolverResult()
   {

   }
   
   public ResolverResult(String jndiName, String refName, String beanName)
   {
      this.jndiName = jndiName;
      this.refName = refName;
      this.beanName = beanName;
   }

   /**
    * Get the resolved global JNDI entry for a component.
    *
    * @return The global JNDI name
    */
   public String getJndiName()
   {
      return jndiName;
   }

   /**
    * Get the resolved MC bean name that will be binding the component into JNDI.
    *
    * @return The MC bean name
    */
   public String getBeanName()
   {
      return beanName;
   }

   @Deprecated
   public String getEncJndiName()
   {
      return getRefName();
   }

   @Deprecated
   public String getGlobalJndiName()
   {
      return getJndiName();
   }

   /**
    * Get the reference name.
    * Since a resolver knows the reference name, this allows polymorphic access.
    */
   public String getRefName()
   {
      return refName;
   }
}
