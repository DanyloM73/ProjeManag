const express = require("express");
const bodyParser = require("body-parser");
const admin = require("firebase-admin");

const serviceAccount = require("./key.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const app = express();

const firestore = admin.firestore();

app.use(bodyParser.json());

app.post("/send-notification", async (req, res) => {
  const {
    userIds,
    title,
    body
  } = req.body;

  try {
    const allTokens = [];

    for (const userId of userIds) {
      const userDoc = await firestore.collection("users").doc(userId).get();
      if (userDoc.exists && userDoc.data().fcmToken) {
        const token = userDoc.data().fcmToken;
        allTokens.push(token);
      }
    }

    const message = {
      data: {
        title: title,
        body: body,
      },
      token: "",
    };

    const notificationPromises = allTokens.map(async (token) => {
      message.token = token
      await admin.messaging().send(message);
    })

    await Promise.all(notificationPromises);

    res.status(200).send("Notification sent");
  } catch (error) {
    console.error("Error sending notification:", error);
    res.status(500).send(error);
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, "0.0.0.0", () => console.log(`Server running on port ${PORT}`));