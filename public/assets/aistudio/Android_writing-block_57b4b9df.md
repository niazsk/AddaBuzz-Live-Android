# ADDABUZZ LIVE — NATIVE ANDROID APPLICATION

Build a complete native Android application for AddaBuzz Live.

This Android application must connect to the existing AddaBuzz Live shared backend.

## TECHNOLOGY

- Kotlin
- Jetpack Compose
- Material 3
- Android Studio
- LiveKit Android SDK
- Supabase-compatible authentication
- HTTPS REST API
- WebSocket / Supabase Realtime integration

## BRANDING

App Name: AddaBuzz Live

Tagline: Connect • Stream • Chat • Enjoy

Use the same branding, logo, typography, colors and visual identity as the AddaBuzz Live Web Application.

## DESIGN

Create a premium dark UI:

- Deep black backgrounds.
- Purple and violet gradients.
- Electric blue accents.
- Glass-style cards.
- Rounded buttons.
- Smooth animations.
- Modern profile avatars.

## SCREENS

1. Splash Screen
2. Login
3. Registration
4. Home
5. Discover
6. Live Categories
7. Live Room
8. Audio Room
9. Multi-Guest Room
10. User Profile
11. Followers
12. Following
13. Private Messages
14. Notifications
15. Wallet
16. Gift Catalog
17. Settings

## AUTHENTICATION

Connect to the existing AddaBuzz Live Supabase authentication system through a secure, supported Android integration.

Support:
- Registration.
- Login.
- Logout.
- Session persistence.
- Google OAuth.
- Password recovery.
- Email verification.

## LIVE STREAMING

Integrate the official LiveKit Android SDK.

Implement:
- Camera publishing.
- Microphone publishing.
- Viewer subscriptions.
- Video Live.
- Audio Live.
- Room joining.
- Room leaving.
- Camera switching.
- Mute controls.
- Network state.
- Reconnection handling.

Retrieve LiveKit tokens securely from the existing backend.

Never embed LiveKit API secrets in the APK.

## MULTI-GUEST ROOM

Support up to 8 guest seats.

Implement:
- Seat display.
- Guest requests.
- Accept/reject.
- Invitations.
- Mute/unmute.
- Remove guest.
- Lock/unlock seats.
- Real-time participant updates.
- Actual LiveKit audio/video tracks.

## SHARED BACKEND

Use the same backend as the Web Application.

Use the existing API endpoints, database schema, authentication rules and permissions.

Do not create an unrelated second database.

The following information must stay synchronized across Android and Web:
- User accounts.
- Profiles.
- Followers.
- Live rooms.
- Participants.
- Messages.
- Notifications.
- Wallet transactions.

## ANDROID PERMISSIONS

Implement appropriate:
- Camera permission.
- Microphone permission.
- Notification permission.
- Internet permission.

Handle Android application lifecycle and streaming disconnection correctly.

## FREE-FIRST RULES

- No mandatory paid SDK.
- No mandatory paid API.
- No premium templates.
- No paid streaming subscription.
- No unnecessary external services.

## PROJECT QUALITY

Create:
- Clean Kotlin architecture.
- Reusable Compose components.
- ViewModels.
- Repository pattern.
- Secure API client.
- Error handling.
- Loading states.
- Permission handling.
- Gradle configuration.
- AndroidManifest.xml.

## BUILD AND TEST

Generate a complete Android Studio project.

Verify:
- Gradle build.
- APK generation.
- Login.
- Registration.
- API connectivity.
- LiveKit connection.
- Camera and microphone permissions.
- Real multi-user streaming.

Do not use mock streaming for production functionality.

The Android application must work with the existing AddaBuzz Live Web backend.

Create the complete Android source project and provide the APK build instructions.

Do not claim that untested integrations are complete.