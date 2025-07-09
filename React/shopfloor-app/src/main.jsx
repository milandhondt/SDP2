import './index.css';
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom';
import NotFound from './pages/NotFound.jsx';
import Layout from './pages/Layout.jsx';
import Dashboard from './pages/Dashboard.jsx';
import SitesOverzicht from './pages/sites/SitesOverzicht.jsx';
import MachinesOverzicht from './pages/machines/MachinesOverzicht.jsx';
import MachineDetail from './pages/machines/MachineDetail.jsx';
import NotificatiesOverzicht from './pages/Notificaties/NotificatiesOverzicht.jsx';
import Login from './pages/Login.jsx';
import SiteDetail from './pages/sites/SiteDetail.jsx';
import SiteGrondplan from './pages/sites/SiteGrondplan.jsx';
import { AuthProvider } from './contexts/Auth.context';
import PrivateRoute from './components/PrivateRoute.jsx';
import AddOrEditSite from './pages/sites/AddOrEditSite.jsx';
import AddOrEditMachine from './pages/machines/AddOrEditMachine.jsx';
import OnderhoudenMachineOverzicht from './pages/onderhouden/OnderhoudenMachineOverzicht.jsx';
import './i18n.js';
import Preferences from './pages/Preferences.jsx';

const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      {
        path: '/',
        element: <PrivateRoute />,
        children: [
          {
            index: true,
            element: <Navigate replace to='/dashboard' />,
          },
        ],
      },
      {
        path: '/preferences',
        element: <PrivateRoute />,
        children: [
          {
            index: true,
            element: <Preferences />,
          },
        ],
      },
      {
        path: '/dashboard',
        element: <PrivateRoute />,
        children: [
          {
            index: true,
            element: <Dashboard />,
          },
        ],
      },
      {
        path: '/sites',
        element: <PrivateRoute />,
        children: [
          {
            index: true,
            element: <SitesOverzicht />,
          },
          {
            path: ':id',
            element: <SiteDetail />,
          },
          {
            path: ':id/grondplan',
            element: <SiteGrondplan />,
          },
          {
            path: ':id/edit',
            element: <AddOrEditSite />,
          },
          {
            path: 'new',
            element: <AddOrEditSite />,
          },
        ],
      },
      {
        path: '/machines',
        element: <PrivateRoute />,
        children: [
          {
            index: true,
            element: <MachinesOverzicht />,
          },
          {
            path: ':id',
            element: <MachineDetail />,
          },
          {
            path: ':id/edit',
            element: <AddOrEditMachine />,
          },
          {
            path: ':id/onderhouden',
            element: <OnderhoudenMachineOverzicht />,
          },
          {
            path: 'new',
            element: <AddOrEditMachine />,
          },
        ],
      },
      {
        path: '/notificaties',
        element: <PrivateRoute />,
        children: [
          {
            index: true,
            element: <NotificatiesOverzicht />,
          },
        ],
      },
      {
        path: '*', element: <NotFound />,
      },
      {
        path: '/not-found',
        element: <NotFound />,
      },
    ],
  },

  {
    path: '/login',
    element: <Login />,
  },
]);

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  </StrictMode>,
);