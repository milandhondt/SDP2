import { Outlet, ScrollRestoration } from 'react-router-dom';
import Navbar from '../components/Navbar';

export default function Layout() {
  return (
    <div className='pt-26 max-sm:pt-38'>
      <Navbar />
      <div className=' container mx-auto px-4 max-sm:p-2'>
        <Outlet />
      </div>
      <ScrollRestoration />
    </div>
  );
}