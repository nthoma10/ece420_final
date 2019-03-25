import cv2
import numpy as np
from matplotlib import pyplot as plt

'''
img_rgb = cv2.imread('circuit.jpg')
img_gray = cv2.cvtColor(img_rgb, cv2.COLOR_BGR2GRAY)
template = cv2.imread('resistor.jpg',0)
w, h = template.shape[::-1]

res = cv2.matchTemplate(img_gray,template,cv2.TM_CCOEFF_NORMED)
print(res)
threshold = -0.3
loc = np.where(res >= threshold)
for pt in zip(*loc[::-1]):
    cv2.rectangle(img_rgb, pt, (pt[0] + w, pt[1] + h), (0,0,255), 2)

cv2.imshow("image", img_rgb)
cv2.waitKey()
'''

img = cv2.imread("circuit.jpg")
gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

template = cv2.imread("resistor.jpg",0)

w, h = template.shape[::-1]

res = cv2.matchTemplate(gray, template, cv2.TM_SQDIFF_NORMED)
min_val, max_val, min_loc, max_loc = cv2.minMaxLoc(res)

print(min_val, max_val)
# create threshold from min val, find where sqdiff is less than thresh
match_locations = np.where(res>=0.23)
print(match_locations)
# draw template match boxes
w, h = template.shape[::-1]
for (x, y) in zip(match_locations[1], match_locations[0]):
    cv2.rectangle(img, (x, y), (x+w, y+h), [0,255,255], 2)

# display result
cv2.imshow('', img)
cv2.waitKey(0)