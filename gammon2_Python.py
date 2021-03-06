
import cv2
import numpy as np
from matplotlib import pyplot as plt

img = cv2.imread("circuit3.jpg")
gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

resistor = cv2.imread("resistor.jpg",0)
resistor90 = cv2.imread("res90.jpg",0)
source = cv2.imread("src.jpg",0)

#cv2.imshow('',img)
wres, hres = resistor.shape[::-1]
wres90, hres90 = resistor90.shape[::-1]
ws, hs = source.shape[::-1]
#cv2.imshow('',gray)

res = cv2.matchTemplate(gray, resistor, cv2.TM_SQDIFF_NORMED)
res90 = cv2.matchTemplate(gray, resistor90, cv2.TM_SQDIFF_NORMED)
src = cv2.matchTemplate(gray, source, cv2.TM_SQDIFF_NORMED)

min_val, max_val, min_loc, max_loc = cv2.minMaxLoc(res)
min_val_90, max_val_90, min_loc_90, max_loc_90 = cv2.minMaxLoc(res90)
min_vals, max_vals, min_locs, max_locs = cv2.minMaxLoc(src)

# create threshold from min val, find where sqdiff is less than thresh
match_locations = np.where(res/max_val>=0.9995)
match_locations_90 = np.where(res90/max_val_90 >= 0.9995)
match_locations_src = np.where(src/max_vals>=0.999)

print("Horizontal Resistors Found: ",len(match_locations[0]))
print("Vertical Resistors Found: ", len(match_locations_90[0]))
print("Voltage Sources Found: ", len(match_locations_src[0]))

# draw template match boxes
for (x, y) in zip(match_locations[1], match_locations[0]):
    cv2.rectangle(img, (x, y), (x+wres, y+hres), [0,255,255], 2)
for (x, y) in zip(match_locations_90[1], match_locations_90[0]):
    cv2.rectangle(img, (x, y), (x+wres90, y+hres90), [0,0,255], 2)
for (x, y) in zip(match_locations_src[1], match_locations_src[0]):
    cv2.rectangle(img, (x, y), (x+ws, y+hs), [255,0,255], 2)
edges = cv2.Canny(gray,50,150,apertureSize = 3)  
minLineLength = 200
maxLineGap = 20
lines = cv2.HoughLinesP(edges,1,np.pi/180,70,minLineLength,maxLineGap)
for i in range(len(lines)):
    for x1,y1,x2,y2 in lines[i]:
        cv2.line(img,(x1,y1),(x2,y2),(0,255,0),2)
    
#cv2.imwrite('houghlines5.jpg',img)

# display result
cv2.imshow('', img)
cv2.waitKey(0)
