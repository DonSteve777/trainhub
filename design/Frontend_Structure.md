# Estructura Frontend - TrainHub

Este documento describe la estructura, componentes y organización del frontend de TrainHub desarrollado en React.

## 1. Arquitectura Frontend

### 1.1 Tecnologías

- **Framework**: React 18+
- **Lenguaje**: TypeScript (recomendado) o JavaScript
- **Gestión de Estado**: React Context API + Hooks
- **Routing**: React Router v6
- **HTTP Client**: Axios o Fetch API
- **WebSocket**: Socket.io-client o WebSocket nativo
- **UI Library**: (Opcional) Material-UI, Ant Design, o CSS custom
- **Build Tool**: Vite o Create React App

### 1.2 Estructura de Carpetas

```
trainhub-frontend/
├─ public/
│  ├─ index.html
│  ├─ favicon.ico
│  └─ assets/              // Imágenes estáticas, logos
│
├─ src/
│  ├─ components/          // Componentes reutilizables
│  │  ├─ common/           // Componentes comunes
│  │  │  ├─ Button/
│  │  │  ├─ Input/
│  │  │  ├─ Modal/
│  │  │  ├─ Loading/
│  │  │  └─ ErrorMessage/
│  │  │
│  │  ├─ layout/           // Componentes de layout
│  │  │  ├─ Navbar/
│  │  │  │  ├─ Navbar.jsx
│  │  │  │  ├─ Navbar.css
│  │  │  │  └─ UserSearch/  // Búsqueda de usuarios
│  │  │  │
│  │  │  ├─ Footer/
│  │  │  └─ Layout/
│  │  │
│  │  └─ post/            // Componentes de publicaciones
│  │     ├─ PostCard/
│  │     │  ├─ PostCard.jsx
│  │     │  ├─ PostCard.css
│  │     │  ├─ PostOverview/  // Vista overview
│  │     │  ├─ PostRunning/  // Vista running times
│  │     │  └─ PostWorkouts/ // Vista workout times
│  │     │
│  │     └─ PostLikeButton/
│  │
│  ├─ pages/              // Páginas/Views principales
│  │  ├─ Login/
│  │  │  ├─ Login.jsx
│  │  │  └─ Login.css
│  │  │
│  │  ├─ Register/
│  │  │  ├─ Register.jsx
│  │  │  └─ Register.css
│  │  │
│  │  ├─ Feed/
│  │  │  ├─ Feed.jsx
│  │  │  └─ Feed.css
│  │  │
│  │  ├─ Profile/
│  │  │  ├─ Profile.jsx
│  │  │  ├─ ProfileEdit/
│  │  │  └─ Profile.css
│  │  │
│  │  ├─ CreatePost/
│  │  │  ├─ CreatePost.jsx
│  │  │  ├─ CreatePostWizard/
│  │  │  │  ├─ StepRunning/
│  │  │  │  ├─ StepWorkouts/
│  │  │  │  └─ StepTotal/
│  │  │  └─ CreatePost.css
│  │  │
│  │  └─ Chat/
│  │     ├─ Chat.jsx
│  │     ├─ ChatWindow/
│  │     ├─ ConversationList/
│  │     ├─ MessageList/
│  │     └─ Chat.css
│  │
│  ├─ services/           // Servicios de API y WebSocket
│  │  ├─ api/
│  │  │  ├─ authService.js
│  │  │  ├─ userService.js
│  │  │  ├─ postService.js
│  │  │  ├─ chatService.js
│  │  │  ├─ searchService.js
│  │  │  └─ apiClient.js    // Configuración Axios
│  │  │
│  │  └─ websocket/
│  │     ├─ chatWebSocket.js
│  │     └─ websocketClient.js
│  │
│  ├─ context/            // React Context
│  │  ├─ AuthContext.jsx
│  │  └─ ThemeContext.jsx  // (Opcional)
│  │
│  ├─ hooks/              // Custom Hooks
│  │  ├─ useAuth.js
│  │  ├─ useWebSocket.js
│  │  ├─ usePagination.js
│  │  └─ useDebounce.js
│  │
│  ├─ utils/              // Utilidades
│  │  ├─ formatters.js    // Formateo de fechas, tiempos
│  │  ├─ validators.js    // Validaciones de formularios
│  │  ├─ constants.js     // Constantes de la aplicación
│  │  └─ storage.js       // (Opcional) Manejo de localStorage
│  │
│  ├─ styles/             // Estilos globales
│  │  ├─ global.css
│  │  ├─ variables.css    // Variables CSS (colores, tamaños)
│  │  └─ reset.css
│  │
│  ├─ App.jsx             // Componente raíz
│  ├─ App.css
│  ├─ index.jsx           // Punto de entrada
│  └─ routes.jsx          // Configuración de rutas
│
├─ package.json
├─ vite.config.js          // o webpack.config.js
└─ README.md
```

## 2. Componentes Principales

### 2.1 Navbar

**Ubicación**: `src/components/layout/Navbar/`

**Funcionalidades**:
- Logo de TrainHub (enlace al Feed)
- Campo de búsqueda de usuarios (se activa al hacer clic en lupa)
- Navegación: Feed, Chat, Perfil
- Avatar y menú de usuario

**Props**:
```javascript
{
  user: User | null,
  onSearch: (query: string) => void,
  onNavigate: (path: string) => void
}
```

**Estado**:
- `searchActive`: boolean (si el campo de búsqueda está activo)
- `searchQuery`: string (término de búsqueda)
- `searchResults`: User[] (resultados de búsqueda)

---

### 2.2 PostCard

**Ubicación**: `src/components/post/PostCard/`

**Funcionalidades**:
- Muestra publicación con formato estilo Instagram
- Deslizamiento horizontal entre 3 vistas:
  - Overview: Resumen general
  - Running: 8 tiempos de running
  - Workouts: 8 tiempos de workouts
- Botón de like
- Contador de likes
- Timestamp relativo

**Props**:
```javascript
{
  post: {
    id: number,
    userId: number,
    r1Time: string,  // Formato MM:SS
    r2Time: string,
    // ... r8Time
    w1Time: string,  // Formato MM:SS
    w2Time: string,
    // ... w8Time
    totalTime: string,  // Formato HH:MM:SS
    likesCount: number,
    isLiked: boolean,
    createdAt: string
  },
  currentUserId: number,
  onLike: (postId: number) => void,
  onNavigateToProfile: (userId: number) => void
}
```

**Subcomponentes**:
- `PostOverview`: Vista general con estadísticas
- `PostRunning`: Tabla con 8 tiempos de running (r1Time a r8Time)
- `PostWorkouts`: Tabla con 8 tiempos de workouts (w1Time a w8Time)

**Nota**: Los tiempos vienen como propiedades individuales del objeto post, no como arrays.

---

### 2.3 CreatePostWizard

**Ubicación**: `src/pages/CreatePost/CreatePostWizard/`

**Funcionalidades**:
- Formulario multi-paso (3 pasos)
- Paso 1: Ingreso de 8 tiempos de running
- Paso 2: Ingreso de 8 tiempos de workouts
- Paso 3: Ingreso de tiempo total
- Validación en cada paso
- Navegación hacia adelante/atrás
- Cancelación con confirmación

**Estado**:
```javascript
{
  currentStep: 1 | 2 | 3,
  r1Time: string,
  r2Time: string,
  r3Time: string,
  r4Time: string,
  r5Time: string,
  r6Time: string,
  r7Time: string,
  r8Time: string,
  w1Time: string,
  w2Time: string,
  w3Time: string,
  w4Time: string,
  w5Time: string,
  w6Time: string,
  w7Time: string,
  w8Time: string,
  totalTime: string,
  errors: ValidationErrors
}
```

**Validaciones**:
- Formato de tiempo: `MM:SS` para tiempos individuales, `HH:MM:SS` para total
- Todos los campos requeridos (8 running + 8 workouts + total)
- Tiempo total >= suma de tiempos individuales

---

### 2.4 ChatWindow

**Ubicación**: `src/pages/Chat/ChatWindow/`

**Funcionalidades**:
- Lista de conversaciones (sidebar izquierdo)
- Área de mensajes (área principal)
- Campo de entrada de mensajes
- Indicadores de estado (enviado, entregado, leído)
- Scroll automático al último mensaje
- Carga paginada de mensajes antiguos

**Estado**:
```javascript
{
  conversations: Conversation[],  // Derivadas de mensajes, identificadas por otherUserId
  selectedOtherUserId: number | null,
  messages: Message[],  // Con senderId y receiverId directamente
  newMessage: string,
  isConnected: boolean,
  unreadCounts: Record<number, number>  // otherUserId -> count
}
```

**Nota**: Las conversaciones se derivan agrupando mensajes por pares de usuarios. No hay `conversationId`; se identifica por el `otherUserId` (el otro usuario del par).

**Subcomponentes**:
- `ConversationList`: Lista de conversaciones
- `MessageList`: Lista de mensajes
- `MessageInput`: Campo de entrada de mensajes
- `MessageBubble`: Burbuja de mensaje individual

---

### 2.5 UserSearch

**Ubicación**: `src/components/layout/Navbar/UserSearch/`

**Funcionalidades**:
- Campo de búsqueda que se activa al hacer clic en lupa
- Búsqueda en tiempo real con debounce (300-500ms)
- Dropdown con resultados
- Mínimo 2 caracteres para buscar
- Indicador de estado de amistad
- Navegación al perfil del usuario

**Props**:
```javascript
{
  onUserSelect: (user: User) => void,
  onClose: () => void
}
```

**Estado**:
```javascript
{
  query: string,
  results: User[],
  isLoading: boolean,
  isOpen: boolean
}
```

---

## 3. Gestión de Estado

### 3.1 AuthContext

**Ubicación**: `src/context/AuthContext.jsx`

**Estado Global**:
```javascript
{
  user: User | null,
  accessToken: string | null,
  isAuthenticated: boolean,
  isLoading: boolean
}
```

**Funciones**:
- `login(email, password)`: Inicia sesión
- `logout()`: Cierra sesión
- `refreshToken()`: Refresca el access token
- `updateUser(userData)`: Actualiza datos del usuario

**Implementación**:
```javascript
// Ejemplo simplificado
const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [accessToken, setAccessToken] = useState(null);
  
  const login = async (email, password) => {
    const response = await authService.login(email, password);
    setAccessToken(response.data.accessToken);
    setUser(response.data.user);
    // Guardar en memoria (no localStorage)
  };
  
  const logout = async () => {
    await authService.logout();
    setAccessToken(null);
    setUser(null);
  };
  
  return (
    <AuthContext.Provider value={{ user, accessToken, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
```

---

### 3.2 Estado Local

**Uso de useState y useReducer**:
- Formularios: Estado local en componentes
- UI temporal: Modales, dropdowns, etc.
- Cache de datos: Opcionalmente React Query o SWR

---

## 4. Servicios de API

### 4.1 apiClient.js

**Configuración base de Axios**:
```javascript
import axios from 'axios';
import { useAuth } from '../context/AuthContext';

const apiClient = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8081/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor para agregar token
apiClient.interceptors.request.use((config) => {
  const token = getAccessToken(); // Desde AuthContext
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor para manejar 401 y refrescar token
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Intentar refrescar token
      const newToken = await refreshAccessToken();
      if (newToken) {
        error.config.headers.Authorization = `Bearer ${newToken}`;
        return apiClient.request(error.config);
      } else {
        // Redirigir a login
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

### 4.2 Servicios Específicos

**authService.js**:
```javascript
import apiClient from './apiClient';

export const authService = {
  register: (data) => apiClient.post('/auth/register', data),
  login: (data) => apiClient.post('/auth/login', data),
  logout: () => apiClient.post('/auth/logout'),
  refreshToken: () => apiClient.post('/auth/refresh'),
  verifyEmail: (token) => apiClient.get(`/auth/verify-email?token=${token}`)
};
```

**postService.js**:
```javascript
import apiClient from './apiClient';

export const postService = {
  create: (data) => {
    // data debe tener: r1Time...r8Time, w1Time...w8Time, totalTime
    return apiClient.post('/posts', data);
  },
  getFeed: (page = 0, size = 20) => 
    apiClient.get(`/posts/feed?page=${page}&size=${size}`),
  getById: (id) => apiClient.get(`/posts/${id}`),
  delete: (id) => apiClient.delete(`/posts/${id}`),
  like: (id) => apiClient.post(`/posts/${id}/like`),
  getLikes: (id, page = 0, size = 20) => 
    apiClient.get(`/posts/${id}/likes?page=${page}&size=${size}`)
};
```

---

## 5. WebSocket para Chat

### 5.1 chatWebSocket.js

**Implementación**:
```javascript
class ChatWebSocket {
  constructor(token) {
    this.token = token;
    this.ws = null;
    this.listeners = new Map();
  }
  
  connect() {
    const wsUrl = `ws://localhost:8081/ws/chat?token=${this.token}`;
    this.ws = new WebSocket(wsUrl);
    
    this.ws.onopen = () => {
      console.log('WebSocket conectado');
      this.emit('connected');
    };
    
    this.ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      this.handleMessage(message);
    };
    
    this.ws.onerror = (error) => {
      console.error('WebSocket error:', error);
      this.emit('error', error);
    };
    
    this.ws.onclose = () => {
      console.log('WebSocket desconectado');
      this.emit('disconnected');
    };
  }
  
  sendMessage(receiverId, content) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify({
        type: 'message.send',
        data: { receiverId, content }
      }));
    }
  }
  
  handleMessage(message) {
    switch (message.type) {
      case 'message.sent':
        this.emit('message.sent', message.data);
        break;
      case 'message.delivered':
        this.emit('message.delivered', message.data);
        break;
      case 'message.read':
        this.emit('message.read', message.data);
        break;
    }
  }
  
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, []);
    }
    this.listeners.get(event).push(callback);
  }
  
  emit(event, data) {
    if (this.listeners.has(event)) {
      this.listeners.get(event).forEach(callback => callback(data));
    }
  }
  
  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }
}

export default ChatWebSocket;
```

### 5.2 useWebSocket Hook

```javascript
import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import ChatWebSocket from '../services/websocket/chatWebSocket';

export const useWebSocket = () => {
  const { accessToken } = useAuth();
  const [ws, setWs] = useState(null);
  const [isConnected, setIsConnected] = useState(false);
  
  useEffect(() => {
    if (accessToken) {
      const chatWs = new ChatWebSocket(accessToken);
      chatWs.connect();
      
      chatWs.on('connected', () => setIsConnected(true));
      chatWs.on('disconnected', () => setIsConnected(false));
      
      setWs(chatWs);
      
      return () => {
        chatWs.disconnect();
      };
    }
  }, [accessToken]);
  
  return { ws, isConnected };
};
```

---

## 6. Utilidades

### 6.1 Formatters

**formatters.js**:
```javascript
export const formatTime = (seconds) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;
  
  if (hours > 0) {
    return `${hours}:${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  }
  return `${minutes}:${String(secs).padStart(2, '0')}`;
};

export const formatRelativeTime = (date) => {
  const now = new Date();
  const diff = now - new Date(date);
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  
  if (minutes < 1) return 'ahora';
  if (minutes < 60) return `hace ${minutes} min`;
  if (hours < 24) return `hace ${hours} h`;
  if (days === 1) return 'ayer';
  if (days < 7) return `hace ${days} días`;
  return new Date(date).toLocaleDateString();
};
```

### 6.2 Validators

**validators.js**:
```javascript
export const validateEmail = (email) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email);
};

export const validatePassword = (password) => {
  const minLength = password.length >= 8;
  const hasUpperCase = /[A-Z]/.test(password);
  const hasNumberOrSymbol = /[0-9!@#$%^&*]/.test(password);
  
  return {
    valid: minLength && hasUpperCase && hasNumberOrSymbol,
    errors: {
      minLength: !minLength ? 'Mínimo 8 caracteres' : null,
      hasUpperCase: !hasUpperCase ? 'Al menos una mayúscula' : null,
      hasNumberOrSymbol: !hasNumberOrSymbol ? 'Al menos un número o símbolo' : null
    }
  };
};

export const validateTimeFormat = (time) => {
  const re = /^(\d{1,2}):([0-5]\d)(:([0-5]\d))?$/;
  return re.test(time);
};
```

---

## 7. Routing

### 7.1 routes.jsx

```javascript
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Login from './pages/Login/Login';
import Register from './pages/Register/Register';
import Feed from './pages/Feed/Feed';
import Profile from './pages/Profile/Profile';
import CreatePost from './pages/CreatePost/CreatePost';
import Chat from './pages/Chat/Chat';
import Layout from './components/layout/Layout';

const PrivateRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? children : <Navigate to="/login" />;
};

const AppRoutes = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        <Route path="/" element={
          <PrivateRoute>
            <Layout />
          </PrivateRoute>
        }>
          <Route index element={<Feed />} />
          <Route path="profile" element={<Profile />} />
          <Route path="create-post" element={<CreatePost />} />
          <Route path="chat" element={<Chat />} />
          <Route path="users/:id" element={<Profile />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
};

export default AppRoutes;
```

---

## 8. Consideraciones de Rendimiento

### 8.1 Optimizaciones

- **Lazy Loading**: Cargar componentes bajo demanda
- **Memoización**: `React.memo`, `useMemo`, `useCallback`
- **Code Splitting**: Separar código por rutas
- **Imágenes**: Lazy loading de imágenes en el feed
- **Paginación**: Cargar contenido bajo demanda

### 8.2 Ejemplo de Lazy Loading

```javascript
import { lazy, Suspense } from 'react';

const Feed = lazy(() => import('./pages/Feed/Feed'));
const Chat = lazy(() => import('./pages/Chat/Chat'));

<Suspense fallback={<Loading />}>
  <Feed />
</Suspense>
```

---

## 9. Referencias

- [API_Design.md](API_Design.md) - Especificación de endpoints
- [Architecture.md](Architecture.md) - Arquitectura del sistema
- [Funcionalidades.md](Funcionalidades.md) - Requisitos funcionales

