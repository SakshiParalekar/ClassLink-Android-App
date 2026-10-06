const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.sendNoteNotification = functions.firestore
    .document('notes/{noteId}')
    .onCreate(async (snap, context) => {
        const newNote = snap.data();
        const category = newNote.category;
        const uploadername = newNote.uploadername;
        const title = newNote.title;

        // Convert the category name to a valid FCM topic name
        const topic = category.replace(/\s+/g, '_').toLowerCase();

        const message = {
            notification: {
                title: 'New Note Uploaded!',
                body: `${uploadername} has uploaded a new note: ${title}`,
            },
            topic: topic,
        };

        try {
            await admin.messaging().send(message);
            console.log('Successfully sent message to topic:', topic);
            return null;
        } catch (error) {
            console.error('Error sending message:', error);
            return null;
        }
    });