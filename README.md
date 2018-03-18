# EMV_PayWave_Reader

Contactless EMV Card Reader

Android project to read an EMV PayWave card using emvtools (https://sourceforge.net/projects/emvtools/).
Project forked from Ian Babington's project (https://github.com/en93/EMV_PayWave_Reader)

Assumptions:
1.	All devices this will run on must have NFC support. The application will not start if the phone does not.
2.	A physical device is required. Emulators will crash on run due to being unable to get an instance of NfcAdapter without hardware. 
3. 	Valid EMV payment cards used (tested with Visa Paywave and Mastercard PayPass cards)

This project is under GNU GPLV3 license

Xavier Stenuit
Ian Babington
	
