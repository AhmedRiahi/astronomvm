package com.astronomvm.kernel.spi;

import com.astronomvm.core.model.meta.component.ComponentMeta;
import com.astronomvm.kernel.exception.ComponentClassNotFoundException;
import com.astronomvm.kernel.components.TestComponent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ComponentRegistryBoardTest {

    @Test
    public void testRegisterComponent(){
        ComponentMeta componentMeta = new ComponentMeta();
        componentMeta.setName("TestComponent");
        ComponentsRegistryBoard.getInstance().registerComponent(componentMeta, TestComponent.class);
        Class clazz = ComponentsRegistryBoard.getInstance().getComponentClass("TestComponent");
        Assert.assertNotNull(clazz);
    }

    @Test(expected = ComponentClassNotFoundException.class)
    public void testGetNotRegisteredComponentClass(){
        Class clazz = ComponentsRegistryBoard.getInstance().getComponentClass("UnknownTestComponent");
    }
}
