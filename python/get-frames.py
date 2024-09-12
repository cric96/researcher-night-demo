import cv2
import time

camera = cv2.VideoCapture(0)

if not camera.isOpened():
    print("Errore: impossibile aprire la camera")
    exit()


delay = 5
frames = 0

while frames < 20:
    ret, frame = camera.read()

    if not ret:
        print("Errore: impossibile leggere il frame")
        break

    file_name = f"frame-{frames}.jpg"

    cv2.imwrite(file_name, frame)

    frames = frames + 1
    time.sleep(delay)

camera.release()
cv2.destroyAllWindows()
