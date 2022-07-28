import os
from fileReader import readImage
from fileReader import readTextFile
import re
import argparse



colorValueSlackRange = 40
blackValueThreshold = 100 # colors below 100 is black
pixelRangeCheck = 4
checkEightSurroundingPixels = True

def atoi(text):
    return int(text) if text.isdigit() else text

def natural_keys(text):
    return [ atoi(c) for c in re.split(r'(\d+)', text) ]

def readFilesFromFolder(directory):
	allFiles = []
	for filename in sorted(os.listdir(directory), key=natural_keys):
		if filename.startswith("GT"):
			filename = os.path.join(directory, filename)
			allFiles.append(readImage(filename))
		elif filename.endswith(".txt"):
			filename = os.path.join(directory, filename)
			allFiles.append(readTextFile(filename))
	return allFiles


def comparePics(studentPic, optimalSegmentPic):
    # for each pixel in studentPic, compare to corresponding pixel in optimalSegmentPic
	global colorValueSlackRange
	global checkEightSurroundingPixels
	global pixelRangeCheck

	height, width = studentPic.shape

	counter = 0 #counts the number of similar pics
	numberOfBlackPixels = 0
	for w in range(width):
		for h in range(height):
			#if any pixel nearby or at the same position is within the range, it counts as correct
			color1 = studentPic[h][w]
			color2 = optimalSegmentPic[h][w]
			if color1 < blackValueThreshold:
				#black color
				numberOfBlackPixels +=1
				if(int(color1) == int(color2)):
					counter +=1
					continue
				elif checkEightSurroundingPixels:
					#check surroundings
					correctFound = False
					for w2 in range(w-pixelRangeCheck, w + pixelRangeCheck + 1):
						if(correctFound):
							break
						for h2 in range(h - pixelRangeCheck, h + pixelRangeCheck + 1):
							if(w2 >=0 and h2 >= 0 and w2 < width and h2 < height):

								color2 = optimalSegmentPic[h2][w2]
								if( color1 - colorValueSlackRange< color2  and color2 < colorValueSlackRange + color1):
									correctFound = True
									counter +=1
									break

	return counter/max(numberOfBlackPixels,1)


def main(**kwargs):
	parser = argparse.ArgumentParser()

	parser.add_argument('--segmented_path', type=str, default='./pareto_front_txt', help='Path to segmented image')
	parser.add_argument('--image_number', type=str, default='86016', help='The number of the training image')
	args = parser.parse_args()

	print(args.image_number)

	training_imgs =  "./training_images/" + args.image_number
	optimalFiles = readFilesFromFolder(training_imgs)
	studentFiles = readFilesFromFolder(args.segmented_path)
	totalScore = 0
	scores = []
	for student in studentFiles:
		highestScore = 0
		for opt in optimalFiles:
			result1 = comparePics(opt[1],student[1])
			result2 = comparePics(student[1],opt[1])
#			print("PRI 1: %.2f" % result1)
#			print("PRI 2: %.2f" % result2)
			result = min(result1,result2)
			highestScore = max(highestScore,result)
		totalScore += highestScore
		a = highestScore*100
		scores.append((student[0], a))
		#print(f"{student[0]} score: %.2f" % a + "%")
	a = totalScore/len(studentFiles)*100
	print(sorted(scores, key=lambda tup: tup[1])[-5:])
	print("Total Average Score: %.2f" % a + "%")

print("Evaluating images, please wait...")
main()
