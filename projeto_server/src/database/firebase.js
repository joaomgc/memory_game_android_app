import admin from "firebase-admin";
import { getFirestore } from "firebase-admin/firestore";
import { readFile } from 'fs/promises';

var serviceAccount = JSON.parse(
  await readFile(
    new URL('../../serviceAccountKey.json', import.meta.url)
  )
);

const firebaseConfig = {
  credential: admin.credential.cert(serviceAccount)
}

export const firebase = admin.apps.length
  ? admin.app()
  : admin.initializeApp(firebaseConfig);

export const db = getFirestore(firebase);
export { admin };
