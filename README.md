# README #

### Purpose ###
Split-It is an Android app that will allow users to easily split costs with their friends,
roommates, business partners, etc. Our app will simplify the task of determining who
owes who what by keeping track of it for you; all you need is your receipts and our app
will do the work for you!

### Scope ###
Our app will run on the Android platform, and we plan to support 80% of retail stores in
the U.S. as well as multiple groups of consumers. The app will utilize the blinkReceipt
API for processing of receipts into a format we can use. Our app may also use a
Venmo API for payments, and use Twillo for user verification. Our project will require a
backend server to store user data and enable users to share and edit receipt data.
Furthermore, the server will need to maintain state between multiple devices in order to
properly handle concurrency issues among users.

### Goals ###
The ultimate goal of this project is to meet all the requirements mentioned in this
document, and have an app that is not only functioning properly, but that brings true
value to the users. Furthermore, we hope that each feature we implement is done so in
the most efficient, and user friendly way possible.
Specifically our goals for this project are being able to successfully extract text from
receipts using OCR, being able to perform appropriate actions given the data read from
the receipt, performing accurate calculations regardless of the number of items or users
in a group, implement profiles and successfully sync the data with the server, give the
user the ability to edit their profile, and finally implement a clean, user friendly UI. In
addition to these goals, we have a few stretch goals that we hope we have time to
implement. These goals include implementing Venmo payments within the app, and
possibly even implementing automatic payments at the end of the month to settle costs.
Our last stretch goal is to have a highly optimized app. OCR is an intensive process,
and we hope that we can optimize this as well as any server communications, so that
there is little noticeable delay for the user.
