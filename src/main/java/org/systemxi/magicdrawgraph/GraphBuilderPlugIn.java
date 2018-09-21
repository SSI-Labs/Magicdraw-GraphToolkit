package org.systemxi.magicdrawgraph;


import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.actions.ActionsID;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.plugins.Plugin;
import scala.App;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GraphBuilderPlugIn extends Plugin {

/**
 * Action, that do all the work.
 */
        private SimpleAction myAction;


/**
 * Initializing the plugin.
 * Create and register action.
 */
        @Override
        public void init()
        {
//javax.swing.JOptionPane.showMessageDialog(null,"my plugin init");
// create action.
            myAction = new SimpleAction("a1","Create GML File");

// configure the action
            AMConfigurator conf = new AMConfigurator()
            {
                public void configure(ActionsManager mngr)
                {

// searching for action after which insert should be done.
                    ActionsCategory actCat = new ActionsCategory("graphtools", "Graph");
                    actCat.setNested(true);
                    mngr.addCategory(actCat);

                    //ActionsCategory category =(ActionsCategory)mngr.getActionFor(ActionsID.TOOLS);

                    actCat.addAction(myAction);

                }

                public int getPriority()
                {
                    return AMConfigurator.MEDIUM_PRIORITY;
                }
            };

//register the configurator
            ActionsConfiguratorsManager.getInstance().addMainMenuConfigurator(conf);


        }

/**
 * Return true always, because this plugin does not have any close specific actions.
 */
        @Override
        public boolean close()
        {
            return true;
        }

/**
 * @see com.nomagic.magicdraw.plugins.Plugin#isSupported()
 */
        @Override
        public boolean isSupported()
        {
            return true;
        }


        class SimpleAction extends MDAction
        {

            private static final long serialVersionUID = 1L;

            public SimpleAction(String id, String name)
            {
                super(id, name, null, null);
            }

            /**
             * Shows Message
             */
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                try {
                    GraphBuilder gb = new GraphBuilder();
                    if(Application.getInstance().getProject()!=null) {
                        gb.GenerateGML();
                    } else {
                        Application.getInstance().getGUILog().log("Load Project First...");
                    }
                    //JOptionPane.showMessageDialog(Application.getInstance().getMainFrame().getParent(), "This is:" + getName());
                } catch (Exception e) {
                    Application.getInstance().getGUILog().log("Error: " + e.toString());
                }
            }

        }
    }


