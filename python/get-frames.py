import cv2
import time

camera = cv2.VideoCapture(4)
camera.set(cv2.CAP_PROP_FRAME_WIDTH, 1920)
camera.set(cv2.CAP_PROP_FRAME_HEIGHT, 1080)

if not camera.isOpened():
    print("Errore: impossibile aprire la camera")
    exit()

delay = 1.5
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
