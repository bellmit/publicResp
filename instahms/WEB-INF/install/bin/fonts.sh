# For Type 2 print we can set the font style in print master.

#The following are Type 2 prints:
#Diagnostic Reports
#Discharge Summary and Patient Documents using Rich Text templates
#Pharmacy Bill

# to make those fonts work we have to install the follwing fonts.

# 1) ttf-bitstream-vera
# 2) freefont
# 3) msttcorefonts

# first two fonts come with linux installation.
# to install the third font use the following command:

sudo apt-get install msttcorefonts

# then it will prompt with some information. press enter.
# after installation make sure all the fonts placed in the following directory
# /usr/share/fonts/truetype
# if not, copy the fonts  and place them in above mentioned directory.
