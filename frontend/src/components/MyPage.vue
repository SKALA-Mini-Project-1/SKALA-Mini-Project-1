<script setup lang="ts">
import { AlertCircle, QrCode, X } from 'lucide-vue-next';
import { ref } from 'vue';

const showQr = ref(false);

const bookings = [
  {
    id: 'T2025-0612-15847',
    title: 'IU 2025 HEREH WORLD TOUR ENCORE',
    date: '2025.06.12',
    time: '18:00',
    seats: 'VIP석 A구역 12열 4번',
    price: 165000,
    status: 'booked',
    statusLabel: '예매완료'
  },
  {
    id: 'T2024-1225-99821',
    title: '2024 PSY ALL NIGHT STAND',
    date: '2024.12.25',
    time: '23:42',
    seats: '스탠딩 가구역 120번',
    price: 143000,
    status: 'used',
    statusLabel: '관람완료'
  }
];
</script>

<template>
  <div class="mx-auto max-w-[1000px] p-3 sm:p-4 md:p-8">
    <h2 class="mb-6 border-b-2 border-[#333] pb-2 text-2xl font-bold text-[#333]">마이페이지</h2>

    <div class="flex flex-col gap-8 md:flex-row">
      <div class="w-full space-y-1 md:w-48">
        <div class="rounded-sm bg-[#f5f5f5] p-3 font-bold text-[#333]">나의 예매</div>
        <button class="w-full p-3 text-left text-sm text-[#666] hover:bg-gray-50 hover:text-[#FF6B00]">예매 내역 확인</button>
        <button class="w-full p-3 text-left text-sm text-[#666] hover:bg-gray-50 hover:text-[#FF6B00]">취소/환불 내역</button>
        <div class="mt-4 rounded-sm bg-[#f5f5f5] p-3 font-bold text-[#333]">회원 정보</div>
        <button class="w-full p-3 text-left text-sm text-[#666] hover:bg-gray-50 hover:text-[#FF6B00]">회원정보 수정</button>
      </div>

      <div class="flex-1">
        <div class="mb-4 flex flex-col items-start gap-1 sm:flex-row sm:items-center sm:justify-between">
          <h3 class="text-lg font-bold text-[#333]">최근 예매 내역</h3>
          <span class="text-xs text-[#666]">최근 3개월 간의 예매 내역입니다.</span>
        </div>

        <div class="space-y-4">
          <div
            v-for="booking in bookings"
            :key="booking.id"
            class="rounded-sm border border-[#e0e0e0] bg-white p-4 shadow-sm transition-shadow hover:shadow-md md:p-6"
          >
            <div class="mb-4 flex flex-col items-start gap-2 border-b border-[#f0f0f0] pb-4 sm:flex-row sm:justify-between">
              <div>
                <div class="mb-1 text-xs text-[#999]">예매번호 {{ booking.id }}</div>
                <h4 class="text-base font-bold text-[#333] md:text-lg">{{ booking.title }}</h4>
              </div>
              <span
                class="rounded-full px-3 py-1 text-xs font-bold"
                :class="booking.status === 'booked' ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 text-gray-500'"
              >
                {{ booking.statusLabel }}
              </span>
            </div>

            <div class="flex flex-col items-start justify-between gap-4 md:flex-row md:items-center">
              <div class="w-full space-y-1 text-sm">
                <div class="flex flex-col gap-0.5 sm:flex-row"><span class="w-16 text-[#666]">관람일시</span><span class="text-[#333]">{{ booking.date }} {{ booking.time }}</span></div>
                <div class="flex flex-col gap-0.5 sm:flex-row"><span class="w-16 text-[#666]">좌석정보</span><span class="text-[#333]">{{ booking.seats }}</span></div>
                <div class="flex flex-col gap-0.5 sm:flex-row"><span class="w-16 text-[#666]">결제금액</span><span class="font-bold text-[#333]">{{ booking.price.toLocaleString() }}원</span></div>
              </div>

              <div v-if="booking.status === 'booked'" class="grid w-full grid-cols-2 gap-2 md:flex md:w-auto md:space-x-2 md:gap-0">
                <button
                  class="flex items-center justify-center rounded-sm bg-[#FF6B00] px-4 py-2 text-sm font-bold text-white hover:bg-[#e56000]"
                  @click="showQr = true"
                >
                  <QrCode :size="16" class="mr-2" />
                  모바일 티켓
                </button>
                <button class="rounded-sm border border-[#ddd] px-4 py-2 text-sm font-bold text-[#666] hover:bg-gray-50">예매취소</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showQr" class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4" @click="showQr = false">
      <div class="w-full max-w-sm overflow-hidden rounded-lg bg-white" @click.stop>
        <div class="flex items-center justify-between bg-[#FF6B00] p-4 text-white">
          <span class="font-bold">모바일 티켓</span>
          <button @click="showQr = false"><X :size="20" /></button>
        </div>
        <div class="flex flex-col items-center p-5 text-center sm:p-8">
          <h3 class="mb-1 text-lg font-bold">IU 2025 HEREH</h3>
          <p class="mb-6 text-sm text-[#666]">2025.06.12 18:00 | VIP석 A구역 12열 4번</p>

          <div class="mb-6 h-40 w-40 rounded-lg bg-gray-900 p-2 sm:h-48 sm:w-48">
            <div class="flex h-full w-full items-center justify-center bg-white"><QrCode :size="120" /></div>
          </div>

          <div class="flex animate-pulse items-center rounded-full bg-red-50 px-3 py-2 text-xs font-bold text-red-500">
            <AlertCircle :size="12" class="mr-1" />
            캡처된 화면으로는 입장이 불가합니다.
          </div>
        </div>
        <div class="border-t border-[#eee] bg-[#f5f5f5] p-4 text-center text-xs text-[#999]">입장 시 직원에게 QR코드를 보여주세요.</div>
      </div>
    </div>
  </div>
</template>
