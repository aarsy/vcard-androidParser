# vcard-androidParser

This is an android library which converts [ez-vcard](http://github.com/mangstadt/ez-vcard) `VCard` object into appropriate Android compatible version so that it can be used to read vcard files easily in android platform

------    

#Download    
###Using Gradle- under dependencies section:   
 
    compile 'com.github.aarsy.vcard-androidParser:vcard-androidParser:1.0.0'

### or Using Maven:

    <dependency>
        <groupId>com.github.aarsy.vcard-androidParser</groupId>
        <artifactId>vcard-androidParser</artifactId>
        <version>1.0.0</version>
        <type>pom</type>
    </dependency>

------

#Documentation

###Reading a vcard or vcf file
  
    File vcardFile = new File(filePath);
    VCardReader reader = null;
    try {
      reader = new VCardReader(vcardFile);
      reader.registerScribe(new AndroidCustomFieldScribe());

      ContactOperations operations = new ContactOperations(context, account_name, account_type);
      
      //insert contacts with specific account_name and their types. For example:
      //both account_name=null and account_type=null if you want to insert contacts into phone
      //you can also pass other accounts
      
      VCard vcard = null;
      while ((vcard = reader.readNext()) != null) {
          operations.insertContact(vcard);
      }
    }catch (Exception e) {
       e.printStackTrace();
    }finally {
      closeQuietly(reader);
    }
    
    See sample for more details..

------    

#Sample Screenshots<br>
<img src="/Screenshots/Screenshot1.png" width="40%" height=90%> | <img src="/Screenshots/Screenshot2.png" width="40%" height=90%>

------

#Compatibility

**Minimum Android SDK**: This library requires a minimum API level of **10**.    

#Special thanks to

   [Mike Angstadt](https://github.com/mangstadt)

