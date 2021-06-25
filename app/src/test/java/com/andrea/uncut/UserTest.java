package com.andrea.uncut;


import com.andrea.uncut.Model.User;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


public class UserTest {

    // Test user Id is retrieved
    @Test
    public void getIdTest(){
        User user = Mockito.mock(User.class);
        Mockito.when(user.getId()).thenReturn("1");
        String id = user.getId();
        Assert.assertEquals("1", id);
    }

    // Test username is retrieved
    @Test
    public void getUsernameTest(){
        User user = Mockito.mock(User.class);
        Mockito.when(user.getUsername()).thenReturn("username");
        String username = user.getUsername();
        Assert.assertEquals("username", username);
    }

    // Test user full name is retrieved
    @Test
    public void getFullnameTest(){
        User user = Mockito.mock(User.class);
        Mockito.when(user.getFullname()).thenReturn("Full Name");
        String fullname = user.getFullname();
        Assert.assertEquals("Full Name", fullname);
    }

    // Test user profile image is retrieved
    @Test
    public void getImageurlTest(){
        User user = Mockito.mock(User.class);
        Mockito.when(user.getImageurl()).thenReturn("https://test.test");
        String imageurl = user.getImageurl();
        Assert.assertEquals("https://test.test", imageurl);
    }

    // Test user bio is retrieved
    @Test
    public void getBioTest(){
        User user = Mockito.mock(User.class);
        Mockito.when(user.getBio()).thenReturn("A short sentence");
        String bio = user.getBio();
        Assert.assertEquals("A short sentence", bio);
    }
}

