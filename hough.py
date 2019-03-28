import cv2
import numpy as np
import random


img = cv2.imread("circuit.jpg")
gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

template = cv2.imread("resistor.jpg",0)

w, h = template.shape[::-1]

res = cv2.matchTemplate(gray, template, cv2.TM_SQDIFF_NORMED)
min_val, max_val, min_loc, max_loc = cv2.minMaxLoc(res)

# create threshold from min val, find where sqdiff is less than thresh
print(min_val,max_val)
match_locations = np.where(res>=0.23)
print(len(match_locations[0]))
# draw template match boxes
w, h = template.shape[::-1]
for (x, y) in zip(match_locations[1], match_locations[0]):
    cv2.rectangle(img, (x, y), (x+w, y+h), [0,255,255], 2)



edges = cv2.Canny(gray,50,150,apertureSize = 3)
minLineLength=100
lines = cv2.HoughLinesP(image=edges,rho=1,theta=np.pi/180, threshold=115,lines=np.array([]), minLineLength=minLineLength,maxLineGap=100)
circles = cv2.HoughCircles(gray, cv2.HOUGH_GRADIENT, 2, 2.2)

a,b,c = lines.shape
print(a)

for i in range(a):
    b = random.randint(0,256)
    g = random.randint(0, 256)
    r = random.randint(0, 256)
    cv2.line(img, (lines[i][0][0], lines[i][0][1]), (lines[i][0][2], lines[i][0][3]), (b, g, r), 3, cv2.LINE_AA)
                    #start point                            #end point

if circles is not None:
    print(circles.shape)
    # convert the (x, y) coordinates and radius of the circles to integers
    circles = np.round(circles[0, :]).astype("int")

    # loop over the (x, y) coordinates and radius of the circles
    for (x, y, r) in circles:

        # draw the circle in the output image, then draw a rectangle
        # corresponding to the center of the circle
        cv2.circle(img, (x, y), r+10, (0, 255, 0), 4)

cv2.imshow('', img)
cv2.waitKey(0)