# HelpMe

# HelpMe is an Android application designed to help people in danger
# By clicking the Help button application gets current user's coordinates and sends it to RESTful web service. Location of user in danger (as well as his name and surname) is then displayed on map to other users and users nearby (<20 km) are notified. 

# Application uses Firebase email and password authentication
# Application uses Google Maps and Google Location Services 

# Project progress:
  #7.5.2018 --> Firebase authentication completed successfully
  #19.5.2018.--> User profile added 
             --> Communication with Firebase database established
             --> Google Maps implemented using Google Maps API
  #29.5.2018. --> Get device location method implemented using Google Location API
              --> RESTful API created usig Django framework (repository HelpMe_API)
  #13.6.2018. --> Get users in danger method implemented (HTTP GET method to RESTful API)
