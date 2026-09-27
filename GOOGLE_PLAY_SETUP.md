# Google Play integration

המפתח של Google Play **לא נמצא בקוד ולא ב-APK**.

GitHub Actions מצפה ל-Secret בשם:

GOOGLE_PLAY_SERVICE_ACCOUNT_JSON

הקובץ play_apps.json מכיל את Package ID של האפליקציות שרוצים להציג.

חשוב: Google Play Developer API אינו מספק endpoint ציבורי שמחזיר "כל האפליקציות בחשבון" בלי לדעת את ה-package name. לכן יש להוסיף כאן את ה-Package ID של כל אפליקציה. לאחר מכן Actions משתמש במפתח המאובטח כדי לקרוא את נתוני ה-release.

ה-API הרשמי מאפשר גישה לנתוני האפליקציות וה-releases של חשבון המפתח; הוא אינו מספק הורדה ציבורית של APK חתום לכל משתמש.
