import cv2
import time

camera = cv2.VideoCapture(0)

if not camera.isOpened():
    print("Errore: impossibile aprire la camera")
    exit()


delay = 1
frames = 0

while frames < 30:
    ret, frame = camera.read()

    if not ret:
        print("Errore: impossibile leggere il frame")
        break

    file_name = f"frame-{frames}.jpg"
    cv2.imwrite(file_name, frame)
    frames = frames + 1
    time.sleep(delay)
    print("Current-frame:", frames)

camera.release()
cv2.destroyAllWindows()
